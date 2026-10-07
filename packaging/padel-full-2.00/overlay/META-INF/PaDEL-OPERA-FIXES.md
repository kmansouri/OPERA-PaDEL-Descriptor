PaDEL-Descriptor OPERA fixed build 2.00
========================================

Upstream PaDEL application version: 2.21
Base binary: padel-full-1.00.jar
Base SHA-256: a9cafc8c520b43eab1ec8ff5c703cba5c98fb698580d8804580bb2c308c3b23c
Patch date: 2026-09-30
Target bytecode: Java 8 (class-file version 52)
Validated runtime: OpenJDK 21.0.11

Corrections:
* AtomValenceTool now publishes a fully initialized immutable valence table.
* A positive -maxruntime no longer starts all descriptor families concurrently.
* Time-limited descriptor calculations run sequentially through Future.get().
* Thread.stop() has been removed from PaDEL descriptor/fingerprint cancellation.
* Calculation failures and timeouts are logged at WARNING level.

Molecule-level parallelism controlled by -threads is retained. Java thread
interruption is cooperative; a legacy descriptor that ignores interruption is
left in an isolated daemon thread rather than being terminated unsafely.
