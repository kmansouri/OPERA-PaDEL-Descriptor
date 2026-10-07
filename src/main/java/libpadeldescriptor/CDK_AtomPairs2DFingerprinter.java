package libpadeldescriptor;

/** Adapter for the CDK 2 atom-pairs 2-D fingerprinter. */
public class CDK_AtomPairs2DFingerprinter extends CDK_Fingerprint {
    public CDK_AtomPairs2DFingerprinter() {
        super();
        cdkFingerprinter_ = new org.openscience.cdk.fingerprint.AtomPairs2DFingerprinter();
        setPrefix("AP2D");
        initDescriptorsValues();
    }
}
