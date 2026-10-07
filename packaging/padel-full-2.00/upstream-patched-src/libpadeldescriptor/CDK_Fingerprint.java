package libpadeldescriptor;

import java.util.BitSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openscience.cdk.fingerprint.IFingerprinter;
import org.openscience.cdk.interfaces.IAtomContainer;

/**
 * Base class for making CDK fingerprint classes runnable.
 *
 * Thread.stop() has been removed; cancellation is cooperative through the
 * executor Future used by libPaDELDescriptorWorker.
 */
public class CDK_Fingerprint extends Thread {

    protected IFingerprinter cdkFingerprinter_;
    protected IAtomContainer molecule_;
    protected String[] descriptorNames_ = null;
    protected String[] descriptorValues_;
    protected String prefix_;
    protected String errorCode_ = "";
    protected int maxFingerprints_ = 0;

    public CDK_Fingerprint() {
    }

    @Override
    public void run() {
        initDescriptorsValues();
        try {
            BitSet fingerprint = cdkFingerprinter_.getFingerprint(molecule_);
            String one = Integer.toString(1);
            String zero = Integer.toString(0);
            int maxFingerprints = getFingerprintCount();
            for (int i = 0; i < maxFingerprints; ++i) {
                descriptorValues_[i] = fingerprint.get(i) ? one : zero;
            }
        } catch (VirtualMachineError ex) {
            throw ex;
        } catch (LinkageError ex) {
            throw ex;
        } catch (Exception ex) {
            Logger.getLogger("global").log(Level.WARNING,
                    "Fingerprinter calculation failed [" + getClass().getName() + "]", ex);
        } finally {
            molecule_ = null;
        }
    }

    @Override
    public void interrupt() {
        super.interrupt();
    }

    public void initDescriptorsValues() {
        int maxFingerprints = getFingerprintCount();
        descriptorValues_ = new String[maxFingerprints];
        for (int i = 0; i < maxFingerprints; ++i) {
            descriptorValues_[i] = errorCode_;
        }
    }

    public int getFingerprintCount() {
        if (maxFingerprints_ == 0) {
            maxFingerprints_ = cdkFingerprinter_.getSize();
        }
        return maxFingerprints_;
    }

    public String[] getDescriptorNames() {
        if (descriptorNames_ == null) {
            int maxFingerprints = getFingerprintCount();
            descriptorNames_ = new String[maxFingerprints];
            for (int i = 0; i < maxFingerprints; ++i) {
                descriptorNames_[i] = prefix_ + Integer.toString(i + 1);
            }
        }
        return descriptorNames_;
    }

    public void setMolecule(IAtomContainer molecule) {
        this.molecule_ = molecule;
        initDescriptorsValues();
    }

    public void setPrefix(String prefix) {
        this.prefix_ = prefix;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode_ = errorCode;
    }
}
