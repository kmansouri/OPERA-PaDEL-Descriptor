package libpadeldescriptor;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.qsar.DescriptorValue;
import org.openscience.cdk.qsar.IMolecularDescriptor;
import org.openscience.cdk.qsar.result.BooleanResult;
import org.openscience.cdk.qsar.result.DoubleArrayResult;
import org.openscience.cdk.qsar.result.DoubleResult;
import org.openscience.cdk.qsar.result.IDescriptorResult;
import org.openscience.cdk.qsar.result.IntegerArrayResult;
import org.openscience.cdk.qsar.result.IntegerResult;

/**
 * Safe adapter around a CDK molecular descriptor.
 *
 * <p>This CDK 2 adapter is deliberately not a {@link Thread}. Molecule-level
 * parallelism is managed by the command-line engine and descriptor families
 * are evaluated sequentially for each molecule.</p>
 */
public class CDK_Descriptor implements Runnable {
    protected IMolecularDescriptor cdkDescriptor_;
    protected IAtomContainer molecule_;
    protected String[] descriptorValues_;
    protected String errorCode_ = "";
    protected int maxDescriptors_ = 0;
    protected Throwable lastError_;

    public CDK_Descriptor() {
    }

    @Override
    public void run() {
        initDescriptorsValues();
        lastError_ = null;
        if (cdkDescriptor_ == null || molecule_ == null) {
            lastError_ = new IllegalStateException("Descriptor or molecule was not initialized");
            return;
        }
        try {
            // CDK 2 descriptors may cache builder-specific services.  A new
            // adapter is created for every molecule, so this call is both safe
            // under parallel execution and correct for the molecule's builder.
            cdkDescriptor_.initialise(molecule_.getBuilder());
            DescriptorValue descriptor = cdkDescriptor_.calculate(molecule_);
            if (descriptor == null) {
                lastError_ = new CDKException("Descriptor returned null DescriptorValue");
                return;
            }
            if (descriptor.getException() != null) {
                lastError_ = descriptor.getException();
                Logger.getLogger(CDK_Descriptor.class.getName()).log(
                        Level.FINE, cdkDescriptor_.getClass().getName(), lastError_);
            }

            String[] values = valuesOf(descriptor.getValue());
            int count = getDescriptorCount();
            for (int i = 0; i < count && i < values.length; i++) {
                String value = values[i] == null ? "" : values[i].trim();
                if (isFiniteValue(value)) {
                    descriptorValues_[i] = value;
                }
            }
            if (values.length != count && lastError_ == null) {
                lastError_ = new CDKException("Descriptor returned " + values.length
                        + " values but declared " + count + ": "
                        + cdkDescriptor_.getClass().getName());
            }
        } catch (Throwable ex) {
            lastError_ = ex;
            Logger.getLogger(CDK_Descriptor.class.getName()).log(
                    Level.FINE, cdkDescriptor_.getClass().getName(), ex);
        }
    }

    private static String[] valuesOf(IDescriptorResult result) {
        if (result == null) return new String[0];
        if (result instanceof DoubleResult) {
            return new String[] {Double.toString(((DoubleResult) result).doubleValue())};
        }
        if (result instanceof IntegerResult) {
            return new String[] {Integer.toString(((IntegerResult) result).intValue())};
        }
        if (result instanceof BooleanResult) {
            return new String[] {Boolean.toString(((BooleanResult) result).booleanValue())};
        }
        if (result instanceof DoubleArrayResult) {
            DoubleArrayResult array = (DoubleArrayResult) result;
            String[] values = new String[array.length()];
            for (int i = 0; i < values.length; i++) values[i] = Double.toString(array.get(i));
            return values;
        }
        if (result instanceof IntegerArrayResult) {
            IntegerArrayResult array = (IntegerArrayResult) result;
            String[] values = new String[array.length()];
            for (int i = 0; i < values.length; i++) values[i] = Integer.toString(array.get(i));
            return values;
        }
        String raw = result.toString();
        return raw == null || raw.trim().isEmpty() ? new String[0] : raw.split(",", -1);
    }

    private static boolean isFiniteValue(String value) {
        if (value == null || value.isEmpty()) return false;
        return !"NaN".equalsIgnoreCase(value)
                && !"Infinity".equalsIgnoreCase(value)
                && !"+Infinity".equalsIgnoreCase(value)
                && !"-Infinity".equalsIgnoreCase(value);
    }

    public final void initDescriptorsValues() {
        int count = getDescriptorCount();
        descriptorValues_ = new String[count];
        Arrays.fill(descriptorValues_, errorCode_);
    }

    public int getDescriptorCount() {
        if (maxDescriptors_ == 0 && cdkDescriptor_ != null) {
            String[] names = cdkDescriptor_.getDescriptorNames();
            if (names != null && names.length > 0) {
                maxDescriptors_ = names.length;
            } else if (cdkDescriptor_.getDescriptorResultType() != null) {
                maxDescriptors_ = cdkDescriptor_.getDescriptorResultType().length();
            }
        }
        return maxDescriptors_;
    }

    public String[] getDescriptorNames() {
        return cdkDescriptor_.getDescriptorNames().clone();
    }

    public String[] getDescriptorValues() {
        return descriptorValues_ == null ? new String[0] : descriptorValues_.clone();
    }

    public IMolecularDescriptor getDescriptor() {
        return cdkDescriptor_;
    }

    public Throwable getLastError() {
        return lastError_;
    }

    public void setMolecule(IAtomContainer molecule) {
        this.molecule_ = molecule;
        initDescriptorsValues();
    }

    public void setErrorCode(String errorCode) {
        this.errorCode_ = errorCode == null ? "" : errorCode;
    }

    public void setParameters(Object[] params) throws CDKException {
        cdkDescriptor_.setParameters(params);
        maxDescriptors_ = 0;
        initDescriptorsValues();
    }
}
