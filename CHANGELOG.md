# Changelog

## v1.0.0-opera-legacy

- Archived the historical OPERA all-in-one PaDEL bundle.
- Identified the embedded application as PaDEL 2.21 with CDK 1.4.15.
- Documented the known timeout/concurrency race that can produce missing values
  and false zero values.

## v2.0.0-opera-fixed-cdk1

- Fixed unsafe CDK valence-table initialization.
- Replaced descriptor-family concurrency within one molecule with sequential
  family evaluation and molecule-level parallelism.
- Removed `Thread.stop()` timeout handling.
- Preserved the 1,444-column current-OPERA descriptor contract.
- Added regression and concurrency validation.
