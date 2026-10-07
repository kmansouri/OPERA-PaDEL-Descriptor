package libpadeldescriptor;


/**
 * Make IPMolecularLearningDescriptor in CDK runnable in a separate thread.
 * 
 * @author yapchunwei
 */
@SuppressWarnings("deprecation")
public class CDK_IPMolecularLearningDescriptor extends CDK_Descriptor
{
    public CDK_IPMolecularLearningDescriptor()
    {
        super();
        cdkDescriptor_ = new org.openscience.cdk.qsar.descriptors.molecular.IPMolecularLearningDescriptor();
        initDescriptorsValues();
    }
}
