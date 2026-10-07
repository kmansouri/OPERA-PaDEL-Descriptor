# OPERA PaDEL-Descriptor

An independently maintained OPERA continuation and modernization of
PaDEL-Descriptor, originally developed by Chun Wei Yap.

> [!NOTE]
> This is not an official release of the original PaDEL author or the Chemistry
> Development Kit project and does not imply their endorsement.

## Release lines

| Tag | Chemistry engine | Intended use | Status |
|---|---|---|---|
| `v1.0.0-opera-legacy` | CDK 1.4.15 | Historical reproduction only | Known concurrency defect |
| `v2.0.0-opera-fixed-cdk1` | Patched CDK 1.4.15 | Existing OPERA models | Stable and recommended |
| `v3.0.0-beta.1-cdk2.13` | CDK 2.13 | Future OPERA retraining and validation | Pre-release |

The historical `padel-full-1.00.jar` is an OPERA packaging version of PaDEL
2.21; it is not PaDEL application version 1.00.

## Which version should I use?

- **Current OPERA models:** use `padel-full-2.00.jar`.
- **Future models retrained with CDK2 descriptors:** evaluate
  `padel-full-3.00-cdk2.jar`.
- **Historical reproduction only:** use `padel-full-1.00.jar`, with awareness of
  its documented missing-value/false-zero defect.

## CDK1-compatible engine: v2

```bash
java -Djava.awt.headless=true \
  -jar padel-full-2.00.jar \
  -2d -removesalt -standardizenitro -detectaromaticity -retainorder \
  -threads 8 -waitingjobs 32 -maxruntime 60000 \
  -dir input.smi -file descriptors.csv
```

## CDK2 engine: v3 beta

```bash
java -Djava.awt.headless=true \
  -jar padel-full-3.00-cdk2.jar \
  -2d -removesalt -standardizenitro -detectaromaticity -retainorder \
  -threads 8 -waitingjobs 32 -maxruntime 60000 -strict \
  -dir input.smi -file descriptors.csv
```

The CDK2 default `-2d` profile retains the historical 1,444 names and order,
but values are not guaranteed to match CDK 1.4. Future OPERA models must be
trained and validated with the exact CDK2 engine/schema used for inference.

## Descriptor profiles in v3

| Profile | Command selection | Columns excluding `Name` |
|---|---|---:|
| Default 1-D/2-D | `-2d` | 1,444 |
| Default plus Kier-Hall counts | `-2d -descriptortypes padel-default-plus-kierhall.xml` | 1,523 |
| All available 1-D/2-D | `-2d -descriptortypes padel-all-2d.xml` | 1,544 |
| Default 3-D | `-3d` | 431 |
| Default PubChem fingerprint | `-fingerprints` | 881 |

## Repository branches and tags

- `main`: current CDK2 development.
- `legacy-cdk1`: maintenance point for the fixed v2 engine.
- `upstream-padel-2.21`: untouched historical source-baseline tag.
- release tags are listed in the table above.

## Documentation

- `BUILDING.md` - build instructions for the CDK2 source tree
- `PORTING_NOTES.md` - CDK 1.x to CDK 2.13 migration notes
- `docs/` - validation, OPERA integration, threading, and provenance
- `schemas/` - versioned descriptor schemas and selection XML
- `release-notes/` - copy-ready GitHub release notes

## Citation

Yap CW. PaDEL-Descriptor: An open source software to calculate molecular
descriptors and fingerprints. *Journal of Computational Chemistry*.
2011;32(7):1466-1474. doi:10.1002/jcc.21707.

Also report the exact release tag, JAR filename, checksum, descriptor-selection
XML, and command-line preprocessing options in derived datasets or models.

## Licensing

This is a mixed-license distribution. Preserve all per-file and third-party
notices. See `LICENSE.md`, `LICENSES/`, `NOTICE.md`, and
`THIRD_PARTY_NOTICES.md`.
