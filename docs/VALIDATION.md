# Validation Report: PaDEL-Descriptor CDK2 3.00

**Release:** `padel-full-3.00-cdk2.jar`  
**Validation date:** 2026-10-01  
**CDK:** 2.13 official all-dependencies release JAR  
**Runtime used for execution testing:** OpenJDK 21.0.11  
**PaDEL compilation target:** Java 8 class-file format (`javac --release 8`)  
**Release JAR SHA-256:** `4219df79770244a2e09bf64bdcdc2bdc12932b3c23d568ca1be61708a282acfc`

## Scope

This release modernizes the PaDEL descriptor engine for future OPERA model development. It is not intended to reproduce CDK 1.4 descriptor values. The command-line contract used by OPERA is retained, and the default 1-D/2-D schema retains the historical 1,444 names and order so that migration can be controlled and audited.

Validation covered build reproducibility, schema integrity, repeated-run determinism, one-worker versus multiworker equivalence, timeout and malformed-input behavior, default and all-enabled descriptor/fingerprint profiles, retained and generated 3-D coordinates, file formats, paths/CSV quoting, output splitting, metadata, and static Java compatibility checks.

This is engineering validation of the software artifact. It is not a scientific validation of every descriptor for every chemical class, and future OPERA models must be trained and externally validated on CDK2-generated values.

## Input provenance

The supplied CDK 2.13 JAR was verified against both supplied checksums:

- SHA-256: `5a42221d412dca53e48fdfc0bfae645310f9de80082800af801d06a87df32411`
- SHA-512: `0c982a73917acbb36a00b7407b49a85b5f88d69ed08849584152d21af56d8b492f1ef0610014f2b67e06475332a8f87d036d035f7b35794b361cd87b2400937d`
- Supplied CDK source ZIP SHA-256: `807b10f94024b215d88852053eb30f48fc9f3a33e08617f1a9b60a9a3bc5f15d`

The reproducible build verifies the CDK JAR checksum before compiling or packaging.

## Modernization and defect corrections

### CDK 2 migration

The PaDEL descriptor/fingerprint layer was ported from legacy CDK 1.x APIs to CDK 2.13, including current atom-container, graph, aromaticity, cycle, SMARTS/SMIRKS, fingerprint, reader, atom-typing, hydrogen, and 3-D builder APIs. The selected PaDEL layer consists of 147 Java source files and produces 172 compiled classes.

### Concurrency and determinism

- Descriptor families are evaluated sequentially within one molecule.
- Different molecules are processed concurrently according to `-threads`.
- CDK 2.13's `AtomValenceTool` still lazily publishes a mutable map. The release overlays it with an immutable table initialized safely during class initialization.
- Information-content accumulation order was made deterministic.
- CDK 2.13 caches mutable `ModelBuilder3D` and `TemplateHandler3D` singletons. `-convert3d` coordinate generation is serialized; descriptor evaluation continues in parallel afterward.
- The CDK ring-template SDF contained 102 invalid negative absolute-isotope records. CDK rejected and ignored them while emitting ERROR messages. The build removes only these invalid records deterministically. Generated coordinates and descriptor CSVs were unchanged before versus after the sanitization.

### Timeouts and failures

- No PaDEL source uses `Thread.stop()` or `ThreadDeath`.
- A positive `-maxruntime` is implemented with `Future.get(timeout)`, cancellation, and cooperative interruption.
- Timeout execution threads are daemon threads and bounded by the selected worker count.
- Failed or timed-out molecules retain their output rows and use the configured missing-value token.
- `-strict` returns exit code 4 when a molecule fails or times out.
- A non-interruptible third-party algorithm cannot be forcibly killed safely inside one JVM; process isolation is required for an absolute hard timeout.

### Additional corrections

- WHIM input and matrix values are checked before eigendecomposition to prevent invalid/hanging calculations.
- Heavy-neighbor degree handling was corrected for explicit versus implicit hydrogens.
- Scalar descriptor error paths return scalar result types.
- `-retain3d` preserves coordinate-bearing input rather than adding coordinate-less explicit hydrogens.
- Missing-value accounting uses an internal sentinel, so metadata remains correct even when the user selects `0` as the output missing token.
- Descriptor-selection XML parsing disables external entities and DTD access.

## Descriptor schemas

| Profile | Descriptor columns, excluding `Name` | Validation result |
|---|---:|---|
| Default 1-D/2-D (`-2d`) | 1,444 | Exact historical names/order; no duplicates |
| All available 1-D/2-D | 1,544 | Validated |
| Default 3-D (`-3d`) | 431 | Validated with retained and generated coordinates |
| Default fingerprint | 881 | Validated |
| All fingerprint/count families | 16,092 | Validated |
| All enabled 2-D + 3-D + fingerprints | 18,067 | Validated |

The default descriptor-selection configuration SHA-256 is:

`326541d7f8aeb4bb3a8abfa362c31e38db69d7a9754943d469e059b217eb939a`

The default 1,444 descriptor names and order were compared directly with the header of the supplied complete legacy PaDEL result and were exactly equal.

## Build validation

- Two clean, offline builds produced byte-for-byte identical 24,153,817-byte JARs.
- Final JAR SHA-256 in both builds: `4219df79770244a2e09bf64bdcdc2bdc12932b3c23d568ca1be61708a282acfc`.
- ZIP/JAR integrity passed.
- No duplicate archive paths were found.
- Shading signatures and `module-info.class` files that would be invalid after repackaging were removed.
- Executable manifest and `Main-Class` were verified.
- The exact release JAR, not only a classpath build, was used in all tests below.

## Functional test matrix

### Supplied three-molecule SMILES file

Command profile: default 1-D/2-D with OPERA-style standardization flags.

- 20 final-JAR runs: 10 with `-threads 1`, 10 with `-threads 8`.
- All 20 CSV files were byte-identical.
- Rows: 3.
- Values: 3 × 1,444 = 4,332.
- Missing values: 0.
- Output SHA-256: `a0719d2978cdb17104b8223fea4ed58cb1b5b85472563aca6b57a293669b7a96`.
- Results were also identical with `-maxruntime -1`, an omitted timeout, and a positive 60-second timeout.

### 120-molecule concurrency stress set

- `-threads 1` and `-threads 8` CSV files were byte-identical.
- Output SHA-256: `574c0991671f70b349a2004cf885e417b24016de239cc042954250aef1d35d4c`.
- Input/output rows: 120/120.
- Failed molecules: 0.
- Timed-out molecules: 0.
- Descriptor-family execution errors: 0.
- Missing values: 104. These were deterministic, molecule-specific mathematically undefined values: 8 sodium-acetate rows × 13 descriptors, not execution failures.
- One worker: 3.11 s elapsed; 222,836 KiB peak RSS on the validation host.
- Eight workers: 1.56 s elapsed; 443,780 KiB peak RSS on the validation host.

Performance and memory numbers are host- and dataset-specific and should not be treated as universal benchmarks.

### Generated 3-D coordinates

- 10 final-JAR runs: 5 with one molecule worker and 5 with four workers.
- All generated-coordinate CSV files were byte-identical.
- Rows: 3.
- Descriptor columns: 431.
- Missing values: 0.
- Standard output and standard error were empty under `-quiet`.
- Output SHA-256: `a8cac903b893606f6c0d4d1dbac3db234e66095309fb3d1a0d873c32a755ada2`.

### Retained 3-D coordinates

- A coordinate-bearing ethane MOL file produced one complete row with 431 descriptors and zero missing values.
- A three-record coordinate-bearing SDF produced three complete rows with preserved titles and zero missing values.

### Fingerprints and expanded profiles

- Default PubChem fingerprint profile: 3 rows × 881 bits, zero missing values.
- All 1-D/2-D families: 3 rows × 1,544 values, zero missing values; one-worker and eight-worker outputs byte-identical.
- All fingerprint/count families: 3 rows × 16,092 values, zero missing values; one-worker and eight-worker outputs byte-identical.
- All enabled families combined: 1 row × 18,067 values, zero missing values.

### Failure paths and exit codes

| Test | Expected | Observed |
|---|---:|---:|
| Mixed valid/invalid SMILES, default mode | 0 | 0 |
| Mixed valid/invalid SMILES, `-strict` | 4 | 4 |
| Forced 1 ms timeout, `-strict` | 4 | 4 |
| Invalid CLI (`-threads 0`) | 2 | 2 |

Malformed-input output retained all three rows and reported one failed/read-error molecule with 1,444 missing values. Forced timeout output retained all three rows and reported three timed-out/failed molecules with 4,332 missing values.

### Input/output behavior

Validated:

- SMILES, MOL, and multi-record SDF input.
- Directory input with deterministic filename ordering.
- UTF-8 CSV output.
- Molecule names containing commas and quotation marks.
- Input and output paths containing spaces.
- Output splitting with `-maxcpdperfile`.
- Legacy properties-file loading with command-line override precedence.
- JSON provenance sidecars.
- Custom missing tokens, including `0`, while retaining correct internal missing-value counts.

## Legacy CDK1 versus CDK2 values

The default schema is intentionally stable, but values are not expected to reproduce CDK 1.4. On the supplied three-molecule test, using a combined absolute/relative tolerance of `1e-12`:

- Total cells: 4,332.
- Textually exact: 3,208.
- Numerically equivalent within tolerance but not textually exact: 916.
- Materially changed: 208 cells across 83 descriptor columns.
- Missing-value mismatches: 0.

This confirms that future OPERA models must regenerate training descriptors and retrain rather than using CDK2 values with historical models.

## Static compatibility checks

- All 172 PaDEL-layer compiled classes have class-file major version 52 (Java 8).
- The fat JAR's non-multi-release base classes are Java 8 or older: majors 46, 48, 49, 50, and 52.
- The official CDK bundle contains 10 Java 9 multi-release classes under `META-INF/versions/9` (major 53), correctly guarded by the multi-release JAR manifest.
- PaDEL-layer `jdeprscan --release 21 --for-removal` reported no use of APIs deprecated for removal.
- Source hygiene scan found no NUL bytes or unexpected control characters.
- Source scan found no `Thread.stop()` or `ThreadDeath` use.

The official all-dependencies CDK JAR contains dormant optional, demo, logging, and legacy XML classes. Whole-bundle `jdeps`/`jdeprscan` therefore reports optional unresolved dependencies, split XML packages, and deprecated APIs in those upstream classes. These are not referenced by the validated PaDEL command-line execution path. The PaDEL layer itself passed the checks above.

## Known limitations

1. Execution testing was performed on OpenJDK 21.0.11. Java 8 bytecode compatibility was checked statically, but the release was not executed on every Java version or vendor JVM.
2. Timeout cancellation is cooperative. Use process isolation in OPERA when an absolute hard kill is required.
3. CDK `ModelBuilder3D` is a legacy coordinate generator. Generated conformations and 3-D descriptors require chemical-domain validation before model training.
4. Some descriptors are mathematically undefined for some structures and can legitimately produce missing values without an execution failure. Metadata records the count.
5. The artifact is an independent modernization, not an official release of the original PaDEL or CDK projects.
6. Model-level predictive validation is outside this software build. Future OPERA models must be retrained and validated using this exact engine/schema or a deliberately versioned successor.

## Recommended future-OPERA invocation

```bash
java -Djava.awt.headless=true \
  -jar padel-full-3.00-cdk2.jar \
  -2d \
  -removesalt \
  -standardizenitro \
  -detectaromaticity \
  -retainorder \
  -threads 8 \
  -waitingjobs 32 \
  -maxruntime 60000 \
  -strict \
  -dir input.smi \
  -file descriptors.csv
```

For a distributed OPERA build, choose an explicit worker count rather than unconstrained maximum mode. A conservative automatic default is `min(molecule count, 8, max(1, available processors - 1))`, with `waitingjobs = 4 × threads`. Record the JAR checksum, descriptor configuration checksum, CDK version, options, and metadata sidecar with every training or inference dataset.
