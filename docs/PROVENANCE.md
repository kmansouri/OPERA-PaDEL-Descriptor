# Provenance and release identities

## Historical PaDEL baseline

- Embedded PaDEL application version: **2.21**
- Legacy chemistry engine: **CDK 1.4.15**
- Historical OPERA fat-JAR filename: `padel-full-1.00.jar`
- `padel-full-1.00.jar` SHA-256:
  `a9cafc8c520b43eab1ec8ff5c703cba5c98fb698580d8804580bb2c308c3b23c`
- Original source bundle SHA-256:
  `f49e29c28a997d1abd316e1c7f874b2124dd796811efb09abfdc6318377c9917`

The `1.00` identifier is an OPERA packaging version, not the original PaDEL
application version.

## Fixed CDK1 compatibility engine

- Release: `padel-full-2.00.jar`
- PaDEL application lineage: 2.21
- Chemistry engine: patched CDK 1.4.15
- SHA-256:
  `ae93a811c906504031c0165b6f722efd8a77a2f131ab78b71cf14e07d367c5e5`

## CDK2 engine

- Release: `padel-full-3.00-cdk2.jar`
- Chemistry engine: CDK 2.13
- SHA-256:
  `4219df79770244a2e09bf64bdcdc2bdc12932b3c23d568ca1be61708a282acfc`
- Official CDK 2.13 JAR SHA-256:
  `5a42221d412dca53e48fdfc0bfae645310f9de80082800af801d06a87df32411`

The CDK2 engine is intended for future OPERA models retrained on CDK2-generated
descriptor values.
