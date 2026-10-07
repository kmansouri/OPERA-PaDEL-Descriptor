#!/usr/bin/env bash
set -euo pipefail

ROOT=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
CDK_JAR="$ROOT/lib/cdk-2.13.jar"
CHECKSUM_FILE="$ROOT/lib/cdk-2.13.sha256"
BUILD="$ROOT/build"
CLASSES="$BUILD/classes"
STAGE="$BUILD/fat-jar"
MANIFEST="$BUILD/MANIFEST.MF"
OUTPUT="$ROOT/padel-full-3.00-cdk2.jar"

for cmd in java javac unzip sha256sum python3; do
    command -v "$cmd" >/dev/null 2>&1 || {
        echo "ERROR: required command not found: $cmd" >&2
        exit 2
    }
done

if [[ ! -f "$CDK_JAR" || ! -f "$CHECKSUM_FILE" ]]; then
    echo "ERROR: CDK 2.13 JAR or checksum is missing under lib/" >&2
    exit 2
fi
(
    cd "$ROOT/lib"
    sha256sum -c "$(basename "$CHECKSUM_FILE")"
)

rm -rf "$CLASSES" "$STAGE" "$BUILD/sources.txt" "$MANIFEST" "$OUTPUT"
mkdir -p "$CLASSES" "$STAGE"
find "$ROOT/src/main/java" -type f -name '*.java' -print | LC_ALL=C sort > "$BUILD/sources.txt"

# Compile the PaDEL layer for the Java 8 class-file format. CDK 2.13's base
# classes use the same format; the bundled JAR also contains optional Java 9
# multi-release entries used by newer runtimes.
javac --release 8 -proc:none -encoding UTF-8 \
    -Xlint:deprecation,unchecked,-options -Werror \
    -cp "$CDK_JAR" \
    -d "$CLASSES" \
    @"$BUILD/sources.txt"

if [[ -d "$ROOT/src/main/resources" ]]; then
    cp -R "$ROOT/src/main/resources/." "$CLASSES/"
fi

# Start from the official all-dependencies CDK 2.13 JAR, remove metadata that
# is invalid after shading, then overlay the rebuilt PaDEL classes. The overlay
# intentionally replaces CDK's AtomValenceTool with the safely initialized
# implementation used by this release.
(
    cd "$STAGE"
    unzip -q "$CDK_JAR"
)
rm -f "$STAGE/META-INF/MANIFEST.MF" "$STAGE/META-INF/INDEX.LIST"
find "$STAGE/META-INF" -type f \( \
    -iname '*.SF' -o -iname '*.RSA' -o -iname '*.DSA' -o -iname 'SIG-*' \
\) -delete 2>/dev/null || true
find "$STAGE" -type f -name 'module-info.class' -delete

# CDK 2.13's ring-template SDF contains invalid negative absolute isotope
# records. The CDK reader ignores them but emits 102 ERROR lines on first 3-D
# coordinate generation. Remove exactly those invalid records deterministically.
python3 "$ROOT/tools/sanitize_cdk_ring_templates.py" \
    "$STAGE/org/openscience/cdk/modeling/builder3d/data/ringTemplateStructures.sdf.gz"

cp -R "$CLASSES/." "$STAGE/"

mkdir -p "$STAGE/META-INF/licenses" "$STAGE/META-INF/padel"
cp "$ROOT/licenses/PaDEL-AGPL-3.0.txt" "$STAGE/META-INF/licenses/"
cp "$ROOT/licenses/CDK-LGPL-2.1-or-later.txt" "$STAGE/META-INF/licenses/"
cp "$ROOT/NOTICE.md" "$STAGE/META-INF/padel/NOTICE.md"
cat > "$STAGE/META-INF/padel/build.properties" <<'EOF'
product=PaDEL-Descriptor CDK2
version=3.00-cdk2
cdk.version=2.13
cdk.sha256=5a42221d412dca53e48fdfc0bfae645310f9de80082800af801d06a87df32411
java.class.target=8
EOF

# Manifest line endings and final blank line are prescribed by the JAR format.
python3 - "$MANIFEST" <<'PY'
from pathlib import Path
import sys
lines = [
    "Manifest-Version: 1.0",
    "Main-Class: padeldescriptor.PaDELDescriptorApp",
    "Implementation-Title: PaDEL-Descriptor CDK2",
    "Implementation-Version: 3.00-cdk2",
    "Implementation-Vendor: PaDEL community modernization build",
    "Specification-Title: PaDEL molecular descriptors",
    "Specification-Version: 3.00",
    "CDK-Version: 2.13",
    "Automatic-Module-Name: org.padel.descriptor.cdk2",
    "Multi-Release: true",
    "",
]
Path(sys.argv[1]).write_bytes("\r\n".join(lines).encode("utf-8") + b"\r\n")
PY

python3 "$ROOT/tools/create_deterministic_jar.py" \
    --stage "$STAGE" --manifest "$MANIFEST" --output "$OUTPUT"

unzip -tq "$OUTPUT" >/dev/null
java -jar "$OUTPUT" -version
sha256sum "$OUTPUT"
echo "Built: $OUTPUT"
