package padeldescriptor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/** Headless command-line entry point for PaDEL-Descriptor CDK2. */
public final class PaDELDescriptorApp {
    private PaDELDescriptorApp() { }

    public static void main(String[] args) {
        int status = run(args);
        if (status != 0) System.exit(status);
    }

    static int run(String[] args) {
        CliOptions options;
        try {
            options = CliOptions.parse(args);
        } catch (Throwable ex) {
            System.err.println("ERROR " + RunLog.concise(ex));
            printUsage(System.err);
            return 2;
        }

        if (options.help) {
            printUsage(System.out);
            return 0;
        }
        if (options.version) {
            System.out.println(BuildInfo.banner());
            System.out.println("Java " + System.getProperty("java.version"));
            return 0;
        }

        File output = options.outputPath == null ? null : new File(options.outputPath).getAbsoluteFile();
        File logFile = options.logResults && output != null
                ? new File(output.getAbsolutePath() + ".log") : null;

        RunLog log = null;
        DescriptorEngine engine = null;
        CsvOutput csv = null;
        ExecutorService workers = null;
        RunSummary summary = new RunSummary();
        DescriptorSchema schema = null;
        try {
            log = new RunLog(options.quiet, logFile);
            schema = DescriptorSchema.load(options);
            final DescriptorSchema schemaRef = schema;
            if (options.listSchemaPath != null) {
                SchemaWriter.write(new File(options.listSchemaPath), schema);
                log.info("Wrote " + schema.names.size() + " schema columns to "
                        + new File(options.listSchemaPath).getAbsolutePath());
                return 0;
            }

            int threads = options.resolvedThreads();
            int waiting = options.resolvedWaitingJobs();
            int capacity = Math.max(threads, threads + waiting);
            log.info(BuildInfo.banner());
            log.info("Descriptor columns: " + schema.names.size()
                    + "; families: " + schema.families.size()
                    + "; configuration SHA-256: " + schema.sha256);
            log.info("Workers: " + threads + "; queued jobs: " + waiting
                    + "; retain order: " + options.retainOrder
                    + "; timeout per molecule: " + options.maxRunTime + " ms");

            csv = new CsvOutput(output, schema.names, options.maxCompoundsPerFile);
            engine = new DescriptorEngine(options, schema, log);
            workers = Executors.newFixedThreadPool(threads, new WorkerThreadFactory());

            final CsvOutput outputRef = csv;
            final DescriptorEngine engineRef = engine;
            final RunSummary summaryRef = summary;
            final RunLog logRef = log;
            final ExecutorService workerRef = workers;

            if (options.retainOrder) {
                final Deque<Pending> pending = new ArrayDeque<Pending>();
                MoleculeReaders.read(new File(options.inputPath), options.useFilenameAsMolName,
                        new MoleculeReaders.Consumer() {
                            @Override public void accept(final MoleculeRecord record) throws Exception {
                                summaryRef.inputMolecules++;
                                while (pending.size() >= capacity) {
                                    Pending first = pending.removeFirst();
                                    store(await(first.future), outputRef, summaryRef, logRef);
                                }
                                Future<CalculationResult> future = workerRef.submit(
                                        safeTask(engineRef, record, schemaRef.names.size(), options.missingValue));
                                pending.addLast(new Pending(future));
                            }
                        }, log);
                while (!pending.isEmpty()) {
                    store(await(pending.removeFirst().future), csv, summary, log);
                }
            } else {
                final CompletionService<CalculationResult> completed =
                        new ExecutorCompletionService<CalculationResult>(workers);
                final AtomicInteger inFlight = new AtomicInteger();
                MoleculeReaders.read(new File(options.inputPath), options.useFilenameAsMolName,
                        new MoleculeReaders.Consumer() {
                            @Override public void accept(final MoleculeRecord record) throws Exception {
                                summaryRef.inputMolecules++;
                                while (inFlight.get() >= capacity) {
                                    store(await(completed.take()), outputRef, summaryRef, logRef);
                                    inFlight.decrementAndGet();
                                }
                                completed.submit(safeTask(engineRef, record,
                                        schemaRef.names.size(), options.missingValue));
                                inFlight.incrementAndGet();
                            }
                        }, log);
                while (inFlight.get() > 0) {
                    store(await(completed.take()), csv, summary, log);
                    inFlight.decrementAndGet();
                }
            }

            summary.endMillis = System.currentTimeMillis();
            log.info("Completed " + summary.outputMolecules + " molecule(s) in "
                    + summary.elapsedMillis() + " ms; successful=" + summary.successfulMolecules
                    + ", failed=" + summary.failedMolecules
                    + ", timed out=" + summary.timedOutMolecules
                    + ", missing values=" + summary.missingValues);
            if (csv.getFileCount() > 1) log.info("Output files: " + csv.getFileCount());
            MetadataWriter.write(output, options, schema, summary,
                    log.getWarningCount(), log.getErrorCount());

            if (options.strict && summary.failedMolecules > 0) return 4;
            return 0;
        } catch (Throwable ex) {
            if (log != null) log.error("Fatal error", ex);
            else System.err.println("ERROR " + RunLog.concise(ex));
            return 3;
        } finally {
            if (workers != null) workers.shutdownNow();
            if (engine != null) engine.close();
            if (csv != null) csv.close();
            if (log != null) log.close();
        }
    }

    private static Callable<CalculationResult> safeTask(final DescriptorEngine engine,
                                                         final MoleculeRecord record,
                                                         final int valueCount,
                                                         final String missingValue) {
        return new Callable<CalculationResult>() {
            @Override public CalculationResult call() {
                try {
                    return engine.calculate(record);
                } catch (Throwable ex) {
                    String[] values = new String[valueCount];
                    java.util.Arrays.fill(values, missingValue == null ? "" : missingValue);
                    return new CalculationResult(record.index, record.name, record.sourceFile,
                            values, 1, valueCount, false, record.readError != null,
                            false, 0L, "Unhandled worker failure: " + RunLog.concise(ex));
                }
            }
        };
    }

    private static CalculationResult await(Future<CalculationResult> future)
            throws InterruptedException, ExecutionException {
        return future.get();
    }

    private static void store(CalculationResult result, CsvOutput output,
                              RunSummary summary, RunLog log) throws IOException {
        output.write(result);
        summary.add(result);
    }

    private static void printUsage(java.io.PrintStream out) {
        out.println(BuildInfo.banner());
        out.println("Usage:");
        out.println("  java -jar padel-full-3.00-cdk2.jar -2d -dir INPUT -file OUTPUT.csv [options]");
        out.println();
        out.println("Descriptor selection:");
        out.println("  -2d                     Calculate the default 1-D/2-D profile");
        out.println("  -3d                     Calculate the default 3-D profile");
        out.println("  -fingerprints           Calculate active fingerprint families");
        out.println("  -descriptortypes FILE   Use a PaDEL descriptor-selection XML file");
        out.println("  -listschema FILE.csv    Write selected column names and exit");
        out.println();
        out.println("Input/output:");
        out.println("  -dir FILE_OR_DIRECTORY  Input SMILES/SDF/MOL or structure directory");
        out.println("  -file OUTPUT.csv        UTF-8 descriptor CSV");
        out.println("  -maxcpdperfile N        Split output after N molecules (0 = unlimited)");
        out.println("  -missingvalue TEXT      Token for unavailable values (default: empty)");
        out.println("  -usefilenameasmolname   Derive generated molecule names from filenames");
        out.println();
        out.println("Molecule preparation:");
        out.println("  -removesalt             Keep the largest connected fragment");
        out.println("  -standardizenitro       Normalize neutral nitro groups");
        out.println("  -standardizetautomers   Apply SMIRKS from -tautomerlist FILE");
        out.println("  -detectaromaticity      Re-perceive aromaticity with CDK 2");
        out.println("  -aromaticitymodel daylight|cdklegacy  (default: daylight)");
        out.println("  -convert3d              Generate 3-D coordinates before 3-D descriptors");
        out.println("  -forcefield NAME        CDK ModelBuilder3D force field (default: mm2)");
        out.println();
        out.println("Execution:");
        out.println("  -threads N              Molecules evaluated in parallel (-1 = all CPUs)");
        out.println("  -waitingjobs N          Maximum queued molecules (default: 4 x threads)");
        out.println("  -maxruntime MS          Per-molecule deadline (-1 = unlimited)");
        out.println("  -retainorder            Preserve input row order");
        out.println("  -noretainorder          Write rows as calculations finish");
        out.println("  -strict                 Exit 4 if any molecule has a calculation error");
        out.println("  -log                    Write OUTPUT.csv.log");
        out.println("  -quiet                  Suppress informational console output");
        out.println("  -config FILE            Load legacy PaDEL properties first");
        out.println("  -version                Print engine/CDK/Java versions");
        out.println("  -help                    Show this help");
        out.println();
        out.println("A JSON provenance sidecar is written as OUTPUT.csv.metadata.json.");
        out.println("Timeout cancellation is cooperative; non-interruptible third-party code is isolated on daemon threads.");
    }

    private static final class Pending {
        final Future<CalculationResult> future;
        Pending(Future<CalculationResult> future) { this.future = future; }
    }

    private static final class WorkerThreadFactory implements ThreadFactory {
        private final AtomicInteger sequence = new AtomicInteger();
        @Override public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, "padel-worker-" + sequence.incrementAndGet());
            thread.setDaemon(false);
            return thread;
        }
    }
}
