package libpadeldescriptor;

import org.openscience.cdk.qsar.descriptors.molecular.AcidicGroupCountDescriptor;

/** Adapter for the CDK acidic group count descriptor. */
public class CDK_AcidicGroupCountDescriptor extends CDK_Descriptor {
    public CDK_AcidicGroupCountDescriptor() {
        super();
        cdkDescriptor_ = new AcidicGroupCountDescriptor();
        initDescriptorsValues();
    }
}
