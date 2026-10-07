# Future OPERA integration

Recommended descriptor command template:

```text
java -Djava.awt.headless=true -jar padel-full-3.00-cdk2.jar \
  -2d -removesalt -standardizenitro -detectaromaticity \
  -retainorder -threads <N> -waitingjobs <4*N> \
  -maxruntime 60000 -dir <structures> -file <descriptors.csv>
```

OPERA should check all of the following before prediction or training:

1. process exit code;
2. output row count;
3. exact expected schema/header hash;
4. per-row field count;
5. missing and nonfinite values according to endpoint policy;
6. metadata `failedMolecules`, `timedOutMolecules`, and `descriptorFamilyErrors`.

For training reproducibility, archive the JAR checksum, command line, descriptor-selection XML, metadata sidecar, Java version, and source structure identifiers with each generated dataset.

The existing OPERA models must continue using the corrected legacy engine (`padel-full-2.00.jar`). Use this CDK2 engine only with models deliberately trained on its output.

## Coordinate generation note

For future 3-D OPERA models, `-convert3d` is supported with multiple molecule workers, but coordinate generation itself is serialized around CDK 2.13's mutable process-wide builder/template state. Descriptor evaluation remains parallel. For maximum reproducibility, record the force field, PaDEL build checksum, CDK version, and descriptor schema checksum with every training dataset.
