# CDK `AtomValenceTool` replacement

The uploaded PaDEL source archive contains `cdk-1.4.15.jar` but not the CDK
source tree. The complete replacement source is therefore supplied at:

`upstream-patched-src/org/openscience/cdk/qsar/AtomValenceTool.java`

The release JAR compiles the relocated equivalent under
`org.openscience.cdk1.qsar`. It preserves the legacy public constructor,
`getValence(IAtom)` API, and the original table values while replacing unsafe
lazy publication with an immutable table initialized by the JVM's class
initialization mechanism.
