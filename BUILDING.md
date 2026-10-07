# Building PaDEL-Descriptor CDK2 from the Git repository

## Requirements

- JDK 11 or newer with `java` and `javac`
- Python 3
- `unzip`
- `sha256sum`
- a POSIX shell

## Obtain CDK 2.13

The Git repository intentionally does not commit the 23 MB third-party CDK JAR.
Download the official `cdk-2.13.jar` release asset and place it at:

```text
lib/cdk-2.13.jar
```

Expected SHA-256:

```text
5a42221d412dca53e48fdfc0bfae645310f9de80082800af801d06a87df32411
```

The checksum file is already present at `lib/cdk-2.13.sha256`.

The downloadable `padel-full-3.00-cdk2-source.zip` GitHub release asset includes
the verified CDK binary and source archive for a complete offline rebuild.

## Build

```bash
./build.sh
```

## Validation

```bash
test/run-smoke-tests.sh
```

For the extended stress test:

```bash
FULL=1 test/run-smoke-tests.sh
```
