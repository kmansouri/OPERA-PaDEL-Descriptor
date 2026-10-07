package libpadeldescriptor;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import libpadeljobs.Worker;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.modeling.builder3d.ModelBuilder3D;
import org.openscience.cdk.modeling.builder3d.TemplateHandler3D;

/**
 * Worker class to calculate descriptors for a molecule.
 *
 * Positive max-runtime values are enforced without launching every descriptor
 * family concurrently and without Thread.stop(). Descriptor families are run
 * sequentially in a daemon executor and the total per-molecule deadline is
 * enforced through Future.get(timeout). Molecule-level parallelism controlled
 * by the -threads option is retained.
 */
public class libPaDELDescriptorWorker extends Worker<libPaDELDescriptorJob> {

    private static final AtomicLong TIMEOUT_THREAD_NUMBER = new AtomicLong(0L);
    private static final long EXECUTOR_SHUTDOWN_WAIT_MILLIS = 250L;

    protected libPaDELDescriptorMaster master = null;
    protected libPaDELDescriptorMasterGeneral masterG = null;
    protected int channelID;
    protected Set<String> descriptorTypes;
    protected List<CDK_Descriptor> cdk_descriptors;
    protected List<CDK_Fingerprint> cdk_fingerprints;
    protected List<CDK_FingerprintCount> cdk_fingerprints_count;

    public libPaDELDescriptorWorker(libPaDELDescriptorMaster master,
                                     int channelID,
                                     LinkedBlockingQueue<libPaDELDescriptorJob> jobsWaiting,
                                     LinkedBlockingQueue<libPaDELDescriptorJob> jobsRunning,
                                     LinkedBlockingQueue<libPaDELDescriptorJob> jobsCompleted,
                                     Set<String> descriptorTypes) {
        super(jobsWaiting, jobsRunning, jobsCompleted);
        this.master = master;
        this.channelID = channelID;
        this.descriptorTypes = descriptorTypes;
    }

    public libPaDELDescriptorWorker(libPaDELDescriptorMasterGeneral masterG,
                                     int channelID,
                                     LinkedBlockingQueue<libPaDELDescriptorJob> jobsWaiting,
                                     LinkedBlockingQueue<libPaDELDescriptorJob> jobsRunning,
                                     LinkedBlockingQueue<libPaDELDescriptorJob> jobsCompleted,
                                     Set<String> descriptorTypes) {
        super(jobsWaiting, jobsRunning, jobsCompleted);
        this.masterG = masterG;
        this.channelID = channelID;
        this.descriptorTypes = descriptorTypes;
    }

    @Override
    public void DoJob() {
        cdk_descriptors = new ArrayList<CDK_Descriptor>();
        cdk_fingerprints = new ArrayList<CDK_Fingerprint>();
        cdk_fingerprints_count = new ArrayList<CDK_FingerprintCount>();
        libPaDELDescriptorType.SetDescriptorTypes(
                descriptorTypes, null, cdk_descriptors,
                cdk_fingerprints, cdk_fingerprints_count);

        reportProgress();

        IAtomContainer molecule = job.getStructure();

        PaDELStandardize standardize = new PaDELStandardize();
        standardize.setRemoveSalt(job.isRemoveSalt());
        standardize.setDearomatize(job.detectAromaticity);
        standardize.setStandardizeTautomers(job.isStandardizeTautomers());
        standardize.setTautomerList(job.getTautomerList());
        standardize.setStandardizeNitro(job.isStandardizeNitro());
        standardize.setRetain3D(job.isRetain3D());
        try {
            molecule = standardize.Standardize(molecule);
        } catch (Exception ex) {
            Logger.getLogger("global").log(Level.WARNING,
                    "Molecule standardization failed for " + job.getName(), ex);
        }

        if (job.isConvertTo3D()) {
            try {
                TemplateHandler3D template = TemplateHandler3D.getInstance();
                ModelBuilder3D mb3d = ModelBuilder3D.getInstance(template, job.getForcefield());
                molecule = (IAtomContainer) mb3d.generate3DCoordinates((IMolecule) molecule, true);
            } catch (Exception ex) {
                Logger.getLogger("global").log(Level.WARNING,
                        "3D coordinate generation failed for " + job.getName(), ex);
            }
        }

        ArrayList<String> descriptors = new ArrayList<String>();
        if (job.getMaxRunTime() <= 0) {
            calculateSequentially(molecule);
        } else {
            calculateSequentiallyWithTimeout(molecule, job.getMaxRunTime());
        }

        for (CDK_Descriptor descriptor : cdk_descriptors) {
            descriptors.addAll(Arrays.asList(descriptor.descriptorValues_));
        }
        for (CDK_Fingerprint fingerprint : cdk_fingerprints) {
            descriptors.addAll(Arrays.asList(fingerprint.descriptorValues_));
        }
        for (CDK_FingerprintCount fingerprintCount : cdk_fingerprints_count) {
            descriptors.addAll(Arrays.asList(fingerprintCount.descriptorValues_));
        }
        job.setDescriptors(descriptors);
    }

    private void reportProgress() {
        if (master != null) {
            String status = "Processing " + job.getName() + " in "
                    + job.getFilename() + " (" + job.getId() + "/"
                    + master.GetMaxMolecules() + "). ";
            if (master.getMolProcessed() > 0) {
                double speed = (System.nanoTime() - master.getStartTime())
                        / 1e9 / master.getMolProcessed();
                status += "Average speed: "
                        + new DecimalFormat("#0.00").format(speed) + " s/mol.";
            }
            master.addMessage(channelID, status);
        }

        if (masterG != null) {
            String status = "Processing " + job.getName() + " (" + job.getId()
                    + "/" + masterG.GetMaxMolecules() + "). ";
            if (masterG.getMolProcessed() > 0) {
                double speed = (System.nanoTime() - masterG.getStartTime())
                        / 1e9 / masterG.getMolProcessed();
                status += "Average speed: "
                        + new DecimalFormat("#0.00").format(speed) + " s/mol.";
            }
            masterG.addMessage(channelID, status);
        }
    }

    private void calculateSequentially(IAtomContainer molecule) {
        for (CDK_Descriptor descriptor : cdk_descriptors) {
            prepareAndRun(descriptor, molecule);
        }
        for (CDK_Fingerprint fingerprint : cdk_fingerprints) {
            prepareAndRun(fingerprint, molecule);
        }
        for (CDK_FingerprintCount fingerprintCount : cdk_fingerprints_count) {
            prepareAndRun(fingerprintCount, molecule);
        }
    }

    private void calculateSequentiallyWithTimeout(IAtomContainer molecule,
                                                   long maxRunTimeMillis) {
        initializeAllResultArrays();

        long startTime = System.nanoTime();
        long timeoutNanos = TimeUnit.MILLISECONDS.toNanos(maxRunTimeMillis);
        ExecutorService executor = Executors.newSingleThreadExecutor(new DaemonThreadFactory());
        boolean completedWithinLimit = true;

        try {
            for (CDK_Descriptor descriptor : cdk_descriptors) {
                if (!prepareAndExecute(executor, descriptor, molecule,
                        startTime, timeoutNanos)) {
                    completedWithinLimit = false;
                    break;
                }
            }

            if (completedWithinLimit) {
                for (CDK_Fingerprint fingerprint : cdk_fingerprints) {
                    if (!prepareAndExecute(executor, fingerprint, molecule,
                            startTime, timeoutNanos)) {
                        completedWithinLimit = false;
                        break;
                    }
                }
            }

            if (completedWithinLimit) {
                for (CDK_FingerprintCount fingerprintCount : cdk_fingerprints_count) {
                    if (!prepareAndExecute(executor, fingerprintCount, molecule,
                            startTime, timeoutNanos)) {
                        completedWithinLimit = false;
                        break;
                    }
                }
            }
        } finally {
            if (completedWithinLimit) {
                executor.shutdown();
            } else {
                executor.shutdownNow();
            }
            awaitExecutorTermination(executor, completedWithinLimit);
        }
    }

    private void initializeAllResultArrays() {
        for (CDK_Descriptor descriptor : cdk_descriptors) {
            descriptor.initDescriptorsValues();
        }
        for (CDK_Fingerprint fingerprint : cdk_fingerprints) {
            fingerprint.initDescriptorsValues();
        }
        for (CDK_FingerprintCount fingerprintCount : cdk_fingerprints_count) {
            fingerprintCount.initDescriptorsValues();
        }
    }

    private boolean prepareAndExecute(ExecutorService executor,
                                      CDK_Descriptor calculator,
                                      IAtomContainer molecule,
                                      long startTime,
                                      long timeoutNanos) {
        try {
            calculator.setMolecule((IAtomContainer) molecule.clone());
        } catch (Exception ex) {
            Logger.getLogger("global").log(Level.WARNING,
                    "Unable to clone molecule for " + calculator.getClass().getName(), ex);
            return hasTimeRemaining(startTime, timeoutNanos);
        }
        return executeWithinDeadline(executor, calculator,
                calculator.getClass().getName(), startTime, timeoutNanos);
    }

    private boolean prepareAndExecute(ExecutorService executor,
                                      CDK_Fingerprint calculator,
                                      IAtomContainer molecule,
                                      long startTime,
                                      long timeoutNanos) {
        try {
            calculator.setMolecule((IAtomContainer) molecule.clone());
        } catch (Exception ex) {
            Logger.getLogger("global").log(Level.WARNING,
                    "Unable to clone molecule for " + calculator.getClass().getName(), ex);
            return hasTimeRemaining(startTime, timeoutNanos);
        }
        return executeWithinDeadline(executor, calculator,
                calculator.getClass().getName(), startTime, timeoutNanos);
    }

    private boolean executeWithinDeadline(ExecutorService executor,
                                          Runnable calculator,
                                          String calculatorName,
                                          long startTime,
                                          long timeoutNanos) {
        long remainingNanos = remainingNanos(startTime, timeoutNanos);
        if (remainingNanos <= 0L) {
            logTimeout(calculatorName);
            return false;
        }

        final Future<?> future;
        try {
            future = executor.submit(calculator);
        } catch (RejectedExecutionException ex) {
            Logger.getLogger("global").log(Level.WARNING,
                    "Descriptor executor rejected " + calculatorName, ex);
            return false;
        }

        try {
            future.get(remainingNanos, TimeUnit.NANOSECONDS);
            return true;
        } catch (TimeoutException ex) {
            future.cancel(true);
            logTimeout(calculatorName);
            return false;
        } catch (InterruptedException ex) {
            future.cancel(true);
            Thread.currentThread().interrupt();
            Logger.getLogger("global").log(Level.WARNING,
                    "Descriptor worker was interrupted while running " + calculatorName, ex);
            return false;
        } catch (ExecutionException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof Error) {
                throw (Error) cause;
            }
            Logger.getLogger("global").log(Level.WARNING,
                    "Descriptor task failed: " + calculatorName, cause);
            return hasTimeRemaining(startTime, timeoutNanos);
        }
    }

    private void prepareAndRun(CDK_Descriptor calculator, IAtomContainer molecule) {
        try {
            calculator.setMolecule((IAtomContainer) molecule.clone());
            calculator.run();
        } catch (VirtualMachineError ex) {
            throw ex;
        } catch (LinkageError ex) {
            throw ex;
        } catch (Exception ex) {
            Logger.getLogger("global").log(Level.WARNING,
                    "Descriptor calculation failed: " + calculator.getClass().getName(), ex);
        }
    }

    private void prepareAndRun(CDK_Fingerprint calculator, IAtomContainer molecule) {
        try {
            calculator.setMolecule((IAtomContainer) molecule.clone());
            calculator.run();
        } catch (VirtualMachineError ex) {
            throw ex;
        } catch (LinkageError ex) {
            throw ex;
        } catch (Exception ex) {
            Logger.getLogger("global").log(Level.WARNING,
                    "Fingerprint calculation failed: " + calculator.getClass().getName(), ex);
        }
    }

    private long remainingNanos(long startTime, long timeoutNanos) {
        return timeoutNanos - (System.nanoTime() - startTime);
    }

    private boolean hasTimeRemaining(long startTime, long timeoutNanos) {
        return remainingNanos(startTime, timeoutNanos) > 0L;
    }

    private void logTimeout(String calculatorName) {
        Logger.getLogger("global").log(Level.WARNING,
                "Maximum runtime exceeded for molecule " + job.getName()
                + " while running " + calculatorName
                + ". Remaining descriptor values were left as the configured error code.");
    }

    private void awaitExecutorTermination(ExecutorService executor,
                                          boolean completedWithinLimit) {
        try {
            if (!executor.awaitTermination(EXECUTOR_SHUTDOWN_WAIT_MILLIS,
                    TimeUnit.MILLISECONDS) && !completedWithinLimit) {
                Logger.getLogger("global").log(Level.WARNING,
                        "A timed-out legacy descriptor did not respond to interruption. "
                        + "It is isolated in a daemon thread so it cannot prevent JVM shutdown.");
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            Logger.getLogger("global").log(Level.WARNING,
                    "Interrupted while stopping the descriptor executor", ex);
        }
    }

    private static final class DaemonThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable,
                    "PaDEL-timeout-worker-" + TIMEOUT_THREAD_NUMBER.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        }
    }
}
