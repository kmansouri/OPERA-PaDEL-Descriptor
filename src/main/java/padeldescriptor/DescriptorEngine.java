package padeldescriptor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import libpadeldescriptor.CDK_Descriptor;
import libpadeldescriptor.CDK_Fingerprint;
import libpadeldescriptor.CDK_FingerprintCount;
import org.openscience.cdk.interfaces.IAtomContainer;

/** Molecule preparation and sequential descriptor-family evaluation. */
final class DescriptorEngine implements AutoCloseable {
    private static final String MISSING_SENTINEL = "\u0000";
    private final CliOptions options;
    private final DescriptorSchema schema;
    private final RunLog log;
    private final List<String> tautomerTransforms;
    private final ExecutorService timeoutExecutor;

    DescriptorEngine(CliOptions options, DescriptorSchema schema, RunLog log) throws Exception {
        this.options = options;
        this.schema = schema;
        this.log = log;
        this.tautomerTransforms = readTautomerTransforms(options.tautomerListPath);
        this.timeoutExecutor = options.maxRunTime > 0
                ? Executors.newFixedThreadPool(options.resolvedThreads(),
                        new NamedDaemonThreadFactory("padel-calc-"))
                : null;
    }

    CalculationResult calculate(final MoleculeRecord record) {
        final long start = System.nanoTime();
        final Progress progress = new Progress(schema.names.size());

        if (record.readError != null || record.molecule == null) {
            return finish(record, progress, start, false, true, false,
                    "Input structure could not be read: " + RunLog.concise(record.readError));
        }

        Callable<BodyResult> body = new Callable<BodyResult>() {
            @Override public BodyResult call() {
                return calculateBody(record, progress);
            }
        };

        if (options.maxRunTime <= 0) {
            BodyResult result = bodyCall(body, progress);
            return finish(record, progress, start, false, false,
                    result.standardizationFailed, result.summary);
        }

        Future<BodyResult> future = timeoutExecutor.submit(body);
        try {
            BodyResult result = future.get(options.maxRunTime, TimeUnit.MILLISECONDS);
            return finish(record, progress, start, false, false,
                    result.standardizationFailed, result.summary);
        } catch (TimeoutException ex) {
            future.cancel(true);
            String message = "Exceeded per-molecule timeout of " + options.maxRunTime + " ms";
            log.warn(record.name + ": " + message);
            return finish(record, progress, start, true, false, false, message);
        } catch (InterruptedException ex) {
            future.cancel(true);
            Thread.currentThread().interrupt();
            String message = "Calculation interrupted";
            return finish(record, progress, start, false, false, false, message);
        } catch (CancellationException ex) {
            String message = "Calculation cancelled";
            return finish(record, progress, start, false, false, false, message);
        } catch (ExecutionException ex) {
            Throwable cause = ex.getCause() == null ? ex : ex.getCause();
            progress.error("Uncaught calculation failure: " + RunLog.concise(cause));
            return finish(record, progress, start, false, false, false,
                    "Uncaught calculation failure: " + RunLog.concise(cause));
        }
    }

    private BodyResult bodyCall(Callable<BodyResult> body, Progress progress) {
        try {
            return body.call();
        } catch (Throwable ex) {
            String message = "Uncaught calculation failure: " + RunLog.concise(ex);
            progress.error(message);
            return new BodyResult(false, message);
        }
    }

    private BodyResult calculateBody(MoleculeRecord record, Progress progress) {
        IAtomContainer molecule;
        IAtomContainer molecule3D;
        try {
            MoleculeStandardizer standardizer = new MoleculeStandardizer(options, tautomerTransforms);
            molecule = standardizer.standardize(record.molecule);
            if (options.compute3D && options.retain3D && !options.convert3D) {
                molecule3D = standardizer.standardizeRetained3D(record.molecule);
            } else {
                molecule3D = molecule;
            }
        } catch (Throwable ex) {
            String message = "Standardization failed: " + RunLog.concise(ex);
            progress.error(message);
            log.warn(record.name + ": " + message);
            return new BodyResult(true, message);
        }

        DescriptorSchema.Bundle bundle;
        try {
            bundle = schema.newBundle();
        } catch (Throwable ex) {
            String message = "Could not initialize descriptor calculators: " + RunLog.concise(ex);
            progress.error(message);
            log.warn(record.name + ": " + message);
            return new BodyResult(false, message);
        }

        int offset = 0;
        for (CDK_Descriptor descriptor : bundle.descriptors) {
            if (Thread.currentThread().isInterrupted()) break;
            descriptor.setErrorCode(MISSING_SENTINEL);
            IAtomContainer familyMolecule = isThreeDimensional(descriptor) ? molecule3D : molecule;
            descriptor.setMolecule(cloneMolecule(familyMolecule, progress, descriptor.getClass().getName()));
            descriptor.run();
            String[] values = descriptor.getDescriptorValues();
            progress.put(offset, values);
            if (descriptor.getLastError() != null) {
                progress.error(descriptor.getClass().getSimpleName() + ": "
                        + RunLog.concise(descriptor.getLastError()));
            }
            offset += values.length;
        }
        for (CDK_Fingerprint fingerprint : bundle.fingerprints) {
            if (Thread.currentThread().isInterrupted()) break;
            fingerprint.setErrorCode(MISSING_SENTINEL);
            fingerprint.setMolecule(cloneMolecule(molecule, progress, fingerprint.getClass().getName()));
            fingerprint.run();
            String[] values = fingerprint.getDescriptorValues();
            progress.put(offset, values);
            if (fingerprint.getLastError() != null) {
                progress.error(fingerprint.getClass().getSimpleName() + ": "
                        + RunLog.concise(fingerprint.getLastError()));
            }
            offset += values.length;
        }
        for (CDK_FingerprintCount count : bundle.fingerprintCounts) {
            if (Thread.currentThread().isInterrupted()) break;
            count.setErrorCode(MISSING_SENTINEL);
            count.setMolecule(cloneMolecule(molecule, progress, count.getClass().getName()));
            count.run();
            String[] values = count.getDescriptorValues();
            progress.put(offset, values);
            if (count.getLastError() != null) {
                progress.error(count.getClass().getSimpleName() + ": "
                        + RunLog.concise(count.getLastError()));
            }
            offset += values.length;
        }

        if (offset != schema.names.size() && !Thread.currentThread().isInterrupted()) {
            progress.error("Internal schema mismatch: wrote " + offset + " of "
                    + schema.names.size() + " values");
        }
        String summary = progress.firstError();
        if (progress.errorCount() > 0) {
            log.warn(record.name + ": " + progress.errorCount()
                    + " descriptor family error(s); first: " + summary);
        }
        return new BodyResult(false, summary);
    }

    private static boolean isThreeDimensional(CDK_Descriptor descriptor) {
        String name = descriptor.getClass().getSimpleName();
        return "CDK_Autocorrelation3DDescriptor".equals(name)
                || "CDK_CPSADescriptor".equals(name)
                || "CDK_GravitationalIndexDescriptor".equals(name)
                || "CDK_LengthOverBreadthDescriptor".equals(name)
                || "CDK_MomentOfInertiaDescriptor".equals(name)
                || "CDK_PetitjeanShapeIndexDescriptor".equals(name)
                || "CDK_RDFDescriptor".equals(name)
                || "CDK_WHIMDescriptor".equals(name);
    }

    private static IAtomContainer cloneMolecule(IAtomContainer molecule, Progress progress,
                                                 String family) {
        try {
            return (IAtomContainer) molecule.clone();
        } catch (CloneNotSupportedException ex) {
            progress.error(family + ": molecule clone failed: " + RunLog.concise(ex));
            return molecule;
        }
    }

    private CalculationResult finish(MoleculeRecord record, Progress progress, long start,
                                     boolean timedOut, boolean readFailed,
                                     boolean standardizationFailed, String summary) {
        String[] values = progress.snapshot();
        int missing = 0;
        for (int i = 0; i < values.length; i++) {
            if (values[i] == null || MISSING_SENTINEL.equals(values[i])) {
                missing++;
                values[i] = options.missingValue;
            }
        }
        long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        return new CalculationResult(record.index, record.name, record.sourceFile, values,
                progress.errorCount(), missing, timedOut, readFailed,
                standardizationFailed, elapsed, summary);
    }

    private static List<String> readTautomerTransforms(String path) throws Exception {
        if (path == null || path.trim().isEmpty()) return java.util.Collections.emptyList();
        List<String> transforms = new ArrayList<String>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(new File(path)), StandardCharsets.UTF_8));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                String value = line.trim();
                if (value.isEmpty() || value.startsWith("#") || value.startsWith("//")) continue;
                transforms.add(value);
            }
        } finally {
            reader.close();
        }
        return transforms;
    }

    @Override
    public void close() {
        if (timeoutExecutor != null) timeoutExecutor.shutdownNow();
    }

    private static final class BodyResult {
        final boolean standardizationFailed;
        final String summary;
        BodyResult(boolean standardizationFailed, String summary) {
            this.standardizationFailed = standardizationFailed;
            this.summary = summary;
        }
    }

    /** Synchronizes family-level progress so timeout snapshots are consistent. */
    private static final class Progress {
        private final String[] values;
        private final AtomicInteger errors = new AtomicInteger();
        private volatile String firstError;

        Progress(int size) {
            this.values = new String[size];
            Arrays.fill(this.values, MISSING_SENTINEL);
        }

        synchronized void put(int offset, String[] familyValues) {
            if (familyValues == null) return;
            int count = Math.min(familyValues.length, values.length - offset);
            if (count > 0) System.arraycopy(familyValues, 0, values, offset, count);
        }

        void error(String message) {
            errors.incrementAndGet();
            if (firstError == null) {
                synchronized (this) {
                    if (firstError == null) firstError = message;
                }
            }
        }

        int errorCount() { return errors.get(); }
        String firstError() { return firstError == null ? "none" : firstError; }
        synchronized String[] snapshot() { return values.clone(); }
    }
}
