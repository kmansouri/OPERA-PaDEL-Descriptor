package libpadeldescriptor;

import java.util.Arrays;
import java.util.BitSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openscience.cdk.fingerprint.IFingerprinter;
import org.openscience.cdk.interfaces.IAtomContainer;

/** Safe, non-Thread adapter around a CDK bit fingerprinter. */
public class CDK_Fingerprint implements Runnable {
    protected IFingerprinter cdkFingerprinter_;
    protected IAtomContainer molecule_;
    protected String[] descriptorNames_;
    protected String[] descriptorValues_;
    protected String prefix_;
    protected String errorCode_ = "";
    protected int maxFingerprints_;
    protected Throwable lastError_;

    public CDK_Fingerprint() {
    }

    @Override
    public void run() {
        initDescriptorsValues();
        lastError_ = null;
        try {
            BitSet fingerprint = cdkFingerprinter_.getBitFingerprint(molecule_).asBitSet();
            for (int i = 0; i < getFingerprintCount(); i++) {
                descriptorValues_[i] = fingerprint.get(i) ? "1" : "0";
            }
        } catch (Throwable ex) {
            lastError_ = ex;
            Logger.getLogger(CDK_Fingerprint.class.getName()).log(Level.FINE,
                    cdkFingerprinter_ == null ? "fingerprint" : cdkFingerprinter_.getClass().getName(), ex);
        }
    }

    public final void initDescriptorsValues() {
        descriptorValues_ = new String[getFingerprintCount()];
        Arrays.fill(descriptorValues_, errorCode_);
    }

    public int getFingerprintCount() {
        if (maxFingerprints_ == 0 && cdkFingerprinter_ != null) {
            maxFingerprints_ = cdkFingerprinter_.getSize();
        }
        return maxFingerprints_;
    }

    public String[] getDescriptorNames() {
        if (descriptorNames_ == null) {
            descriptorNames_ = new String[getFingerprintCount()];
            for (int i = 0; i < descriptorNames_.length; i++) {
                descriptorNames_[i] = prefix_ + (i + 1);
            }
        }
        return descriptorNames_.clone();
    }

    public String[] getDescriptorValues() {
        return descriptorValues_ == null ? new String[0] : descriptorValues_.clone();
    }

    public Throwable getLastError() {
        return lastError_;
    }

    public void setMolecule(IAtomContainer molecule) {
        molecule_ = molecule;
        initDescriptorsValues();
    }

    public void setPrefix(String prefix) {
        prefix_ = prefix;
        descriptorNames_ = null;
    }

    public void setErrorCode(String errorCode) {
        errorCode_ = errorCode == null ? "" : errorCode;
    }
}
