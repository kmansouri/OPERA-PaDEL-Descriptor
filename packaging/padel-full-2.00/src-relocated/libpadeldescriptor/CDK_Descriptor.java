package libpadeldescriptor;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.openscience.cdk1.exception.CDKException;
import org.openscience.cdk1.interfaces.IAtomContainer;
import org.openscience.cdk1.qsar.DescriptorValue;
import org.openscience.cdk1.qsar.IMolecularDescriptor;

/**
 * Base class for making CDK descriptor classes runnable.
 *
 * The legacy implementation forcibly terminated calculations with
 * Thread.stop(). That operation is unsafe and throws
 * UnsupportedOperationException on modern Java releases. Cancellation is now
 * cooperative and is controlled by the Future used by
 * libPaDELDescriptorWorker.
 */
public class CDK_Descriptor extends Thread {

    protected IMolecularDescriptor cdkDescriptor_;
    protected IAtomContainer molecule_;
    protected String[] descriptorValues_;
    protected String errorCode_ = "";
    protected int maxDescriptors_ = 0;

    public CDK_Descriptor() {
    }

    /**
     * Calculate the descriptor and leave the configured error code in any
     * value that cannot be calculated.
     */
    @Override
    public void run() {
        initDescriptorsValues();
        try {
            DescriptorValue descriptor = cdkDescriptor_.calculate(molecule_);
            if (descriptor == null) {
                logFailure("Descriptor returned no result", null);
                return;
            }

            if (descriptor.getException() != null) {
                logFailure("Descriptor reported an exception", descriptor.getException());
            }

            if (descriptor.getValue() == null) {
                logFailure("Descriptor returned a null value", null);
                return;
            }

            String[] valueStr = descriptor.getValue().toString().split(",", -1);
            int maxDescriptors = getDescriptorCount();
            int valuesToCopy = Math.min(maxDescriptors, valueStr.length);

            for (int i = 0; i < valuesToCopy; ++i) {
                String value = valueStr[i];
                if (value != null && !"NaN".equals(value)) {
                    descriptorValues_[i] = value;
                }
            }

            if (valueStr.length < maxDescriptors) {
                logFailure("Descriptor returned " + valueStr.length
                        + " values; " + maxDescriptors + " were expected", null);
            }
        } catch (VirtualMachineError ex) {
            throw ex;
        } catch (LinkageError ex) {
            throw ex;
        } catch (Exception ex) {
            logFailure("Descriptor calculation failed", ex);
        } finally {
            molecule_ = null;
        }
    }

    /**
     * Request cooperative cancellation. No asynchronous force-stop is used.
     */
    @Override
    public void interrupt() {
        super.interrupt();
    }

    private void logFailure(String message, Throwable error) {
        String descriptorName = cdkDescriptor_ == null
                ? getClass().getName() : cdkDescriptor_.getClass().getName();
        String fullMessage = message + " [" + descriptorName + "]";
        if (error == null) {
            Logger.getLogger("global").log(Level.WARNING, fullMessage);
        } else {
            Logger.getLogger("global").log(Level.WARNING, fullMessage, error);
        }
    }

    public void initDescriptorsValues() {
        int maxDescriptors = getDescriptorCount();
        descriptorValues_ = new String[maxDescriptors];
        for (int i = 0; i < maxDescriptors; ++i) {
            descriptorValues_[i] = errorCode_;
        }
    }

    public int getDescriptorCount() {
        if (maxDescriptors_ == 0) {
            maxDescriptors_ = cdkDescriptor_.getDescriptorResultType().length();
        }
        return maxDescriptors_;
    }

    public String[] getDescriptorNames() {
        return cdkDescriptor_.getDescriptorNames();
    }

    public String[] getDescriptorValues() {
        return descriptorValues_.clone();
    }

    public void setMolecule(IAtomContainer molecule) {
        this.molecule_ = molecule;
        initDescriptorsValues();
    }

    public void setErrorCode(String errorCode) {
        this.errorCode_ = errorCode;
    }

    public void setParameters(Object[] params) throws CDKException {
        cdkDescriptor_.setParameters(params);
    }
}
