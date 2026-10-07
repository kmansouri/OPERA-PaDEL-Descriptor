# padel-full-2.00 validation report

Validation date: 2026-09-30

## Release identity

- Release JAR: `padel-full-2.00.jar`
- Release JAR SHA-256: `ae93a811c906504031c0165b6f722efd8a77a2f131ab78b71cf14e07d367c5e5`
- Validated base JAR SHA-256: `a9cafc8c520b43eab1ec8ff5c703cba5c98fb698580d8804580bb2c308c3b23c`
- Upstream application resource version: PaDEL 2.21
- Fixed build identifier: 2.00

## Environment

- OpenJDK runtime: 21.0.11
- Compiler: javac 21.0.11 using `--release 8`
- Patched class-file version: 52 (Java 8)
- Operating path: headless command-line PaDEL descriptor calculation

## Defect reproduction against the supplied base

With the original JAR, `-threads 1 -maxruntime 60000` still launched descriptor
families concurrently. In 20 independent executions of the three-molecule test,
5 runs contained missing descriptor blocks of 26 or 91 values. In a separate
parallel stress set, the original JAR produced 24,182 blank fields and logged
`AtomValenceTool` initialization failures. This reproduced the user's random
missing-block symptom before any patch was applied.

## Tests on the exact release JAR

### Formerly unsafe positive-timeout route

Command settings: 2D descriptors, standard PaDEL preprocessing, retain order,
`-threads 1 -maxruntime 60000`.

Result for the supplied three-molecule file:

- 3 rows
- 1,444 descriptor columns per row
- 0 blank or non-finite descriptor fields
- 0 bytes written to standard error

### Parallel positive-timeout stress test

Command settings: 150 valid structures, `-threads 8 -maxruntime 60000`.

Result:

- 150 rows
- 1,444 descriptor columns per row
- 0 blank or non-finite descriptor fields
- 0 bytes written to standard error

### Parallel unlimited stress test

Command settings: the same 150 structures, `-threads 8 -maxruntime -1`.

Result:

- 150 rows
- 1,444 descriptor columns per row
- 0 blank or non-finite descriptor fields
- 0 bytes written to standard error

### Numerical regression comparison

The release output for the supplied three structures was compared field by
field with the original JAR's reliable single-threaded, no-positive-timeout
baseline.

- Semantic mismatches above `1e-10`: 0
- Maximum absolute numeric difference: `1.4210854715202004e-14`

The observed maximum is ordinary floating-point last-bit variation.

### Forced timeout

A deliberately unrealistic `-maxruntime 1` was used to exercise cancellation.
All three molecules produced an explicit timeout warning and the JVM exited
normally. No `UnsupportedOperationException`, `Thread.stop`, or `ThreadDeath`
message occurred. Remaining values were left blank, as configured, rather than
being silently presented as completed calculations.

## Static checks

- All nine generated/overlaid class files use class-file major version 52.
- No patched bytecode reference to `java/lang/Thread.stop` or
  `java/lang/ThreadDeath` was found.
- `jdeprscan --release 21 --for-removal`, with the release JAR supplied as the
  dependency class path, reported no deprecated-for-removal API in the patched
  classes.
- `unzip -t` passed.
- Duplicate archive paths: 0.
- The embedded manifest records the fixed build version, Java 8 class target,
  and OpenJDK 21 validation runtime.

## Corrections covered by these tests

1. Safe immutable initialization of the legacy CDK valence table.
2. Sequential descriptor-family evaluation within each molecule even when
   `-maxruntime` is positive.
3. Safe timeout coordination through `ExecutorService`, `Future.get`, and
   cooperative interruption instead of `Thread.stop()`.
4. Preserved molecule-level parallelism through `-threads`.
5. Correct scalar dummy-result types for three legacy atomic descriptors.
6. Visible warning-level reporting for descriptor failures and timeouts.

## Scope and residual limitations

The headless descriptor CLI was tested. The fat JAR still contains legacy
third-party libraries and old GUI/native code that were not modernized in this
focused repair. Compatibility with every future Java release cannot be proven
in advance. The known timeout incompatibility on Java 20 and later is removed,
and the exact release was executed successfully on OpenJDK 21.0.11.

Java interruption is cooperative. A third-party descriptor that ignores an
interrupt may continue temporarily in an isolated daemon thread after its
molecule has timed out. It cannot prevent normal JVM shutdown, but a strict
hard kill of arbitrary non-cooperative code requires process isolation.
