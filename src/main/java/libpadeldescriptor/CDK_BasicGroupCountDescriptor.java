package libpadeldescriptor;

import org.openscience.cdk.qsar.descriptors.molecular.BasicGroupCountDescriptor;

/** Adapter for the CDK basic group count descriptor. */
public class CDK_BasicGroupCountDescriptor extends CDK_Descriptor {
    public CDK_BasicGroupCountDescriptor() {
        super();
        cdkDescriptor_ = new BasicGroupCountDescriptor();
        initDescriptorsValues();
    }
}
