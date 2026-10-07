# padel-full-2.00 — OPERA/PaDEL concurrency and modern-Java repair

This package contains the exact Java sources used to produce
`padel-full-2.00.jar` from the supplied `padel-full-1.00.jar`.

## Validated base

- Base file: `padel-full-1.00.jar`
- SHA-256: `a9cafc8c520b43eab1ec8ff5c703cba5c98fb698580d8804580bb2c308c3b23c`
- Upstream PaDEL application resource version: 2.21
- Fixed build version: 2.00
- Compiled class-file target: Java 8 / major version 52
- Runtime used for validation: OpenJDK 21.0.11

The base binary relocates CDK classes from `org.openscience.cdk` to
`org.openscience.cdk1`. `src-relocated/` is therefore the source compiled into
the fat JAR. `upstream-patched-src/` provides the same changes in the ordinary
unrelocated upstream namespace for code review or integration into a normal
PaDEL/CDK source tree.

## Corrections

1. `AtomValenceTool` now creates and safely publishes a complete immutable
   valence table during class initialization.
2. A positive `-maxruntime` no longer starts every descriptor family at once.
   Families run sequentially for each molecule under one total deadline.
3. `Thread.stop()` was removed. Timeout cancellation uses `Future.cancel(true)`
   and cooperative interruption.
4. Descriptor and fingerprint failures are logged at `WARNING` level and leave
   their configured error value rather than crashing a worker silently.
5. Three legacy atomic descriptors now return a scalar `DoubleResult` on error,
   matching their declared and successful result type.
6. Molecule-level parallelism controlled by `-threads` is retained.

## Build

```bash
./build_padel_full_2.00.sh /path/to/padel-full-1.00.jar \
  /path/to/padel-full-2.00.jar
```

The script verifies the base SHA-256, compiles the seven patched classes for
Java 8 compatibility, overlays the resulting class files and metadata, and
checks the resulting ZIP/JAR structure.

## Recommended command

The corrected timeout path can be used with multiple molecule workers:

```bash
java -Djava.awt.headless=true -jar padel-full-2.00.jar \
  -2d -removesalt -standardizenitro -detectaromaticity -retainorder \
  -threads 8 -maxruntime 60000 \
  -dir input.smi -file descriptors.csv
```

Choose `-threads` according to available cores and memory. `-maxruntime` is a
total limit per molecule, in milliseconds, matching the legacy command-line
meaning.

## Timeout limitation

Java interruption is cooperative. If a legacy third-party descriptor ignores
interruption, it cannot be force-killed safely inside the same JVM. The fixed
worker abandons such a timed-out calculation in an isolated daemon executor so
it cannot block JVM shutdown. A strict adversarial hard timeout requires a
separate worker process that can be terminated at the operating-system level.
For normal PaDEL descriptor calculations and OPERA's 60-second setting, the
validated tests did not encounter this condition.

## Compatibility scope

The known Java 20+ incompatibility caused by `Thread.stop()` has been removed.
The fixed classes contain no reference to that API and were tested on OpenJDK
21. The package cannot promise compatibility with every future JVM or every
unused GUI/native-library path in the legacy fat JAR. The headless descriptor
CLI, including positive timeouts and molecule-level parallelism, is the
validated path.

## Files

- `src-relocated/`: exact sources compiled into the provided fat JAR
- `upstream-patched-src/`: equivalent unrelocated PaDEL/CDK sources
- `upstream-source.patch`: unified diff for the six PaDEL source files
- `overlay/`: version and build-notice resources embedded in the JAR
- `manifest-add.mf`: manifest attributes added to the JAR
- `validation.md`: tests performed on the release artifact
- `verify_descriptor_csv.py`: optional blank/non-finite field checker
- `licenses/`: license notices shipped with the supplied distribution

This is a modified build of legacy PaDEL/CDK code. The included source files
retain their upstream notices; consult `licenses/` for the bundled license text.
