package padeldescriptor;

/** Build and engine identity embedded in the release. */
public final class BuildInfo {
    public static final String PRODUCT = "PaDEL-Descriptor CDK2";
    public static final String VERSION = "3.00-cdk2";
    public static final String CDK_VERSION = "2.13";
    public static final String DESCRIPTOR_PROFILE = "PaDEL legacy 1D/2D schema on CDK 2.13";

    private BuildInfo() {
    }

    public static String banner() {
        return PRODUCT + " " + VERSION + " (CDK " + CDK_VERSION + ")";
    }
}
