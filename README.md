# OPERA PaDEL-Descriptor

An independently maintained OPERA continuation of PaDEL-Descriptor, originally
developed by Chun Wei Yap.

## Releases

| Tag | Engine | Intended use | Status |
|---|---|---|---|
| `v1.0.0-opera-legacy` | PaDEL 2.21 / CDK 1.4.15 | Historical reproduction | Known defect; archival only |
| `v2.0.0-opera-fixed-cdk1` | PaDEL 2.21 / patched CDK 1.4.15 | Current OPERA models | Stable and recommended |

## Current OPERA engine

Use `padel-full-2.00.jar` for current OPERA models. It preserves the historical
1,444-column schema and fixes the legacy valence-table and timeout/concurrency
failures.

```bash
java -Djava.awt.headless=true \
  -jar padel-full-2.00.jar \
  -2d -removesalt -standardizenitro -detectaromaticity -retainorder \
  -threads 8 -waitingjobs 32 -maxruntime 60000 \
  -dir input.smi -file descriptors.csv
```

## Roadmap

A separate CDK 2.13 engine is being developed for future OPERA models that will
be retrained on the new descriptor values.

## Citation and licensing

Cite the original PaDEL paper and identify the exact release tag/JAR checksum.
See `CITATION.cff`, `NOTICE.md`, and `LICENSE.md`.
