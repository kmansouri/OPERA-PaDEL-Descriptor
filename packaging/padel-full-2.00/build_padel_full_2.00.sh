#!/usr/bin/env bash
set -euo pipefail

EXPECTED_BASE_SHA256="a9cafc8c520b43eab1ec8ff5c703cba5c98fb698580d8804580bb2c308c3b23c"

usage() {
  cat <<USAGE
Usage: $0 /path/to/padel-full-1.00.jar [/path/to/padel-full-2.00.jar]

Builds the fixed fat JAR by compiling the patched Java 8-compatible classes
and overlaying them on the exact padel-full-1.00.jar used for validation.
Set ALLOW_UNVERIFIED_BASE=1 only when deliberately testing a different base.
USAGE
}

if [[ $# -lt 1 || $# -gt 2 ]]; then
  usage >&2
  exit 2
fi

BASE_JAR=$(cd "$(dirname "$1")" && pwd)/$(basename "$1")
OUTPUT_JAR=${2:-"$(pwd)/padel-full-2.00.jar"}
SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)

for tool in java javac jar unzip sha256sum find; do
  command -v "$tool" >/dev/null 2>&1 || {
    echo "Required tool not found: $tool" >&2
    exit 1
  }
done

[[ -f "$BASE_JAR" ]] || { echo "Base JAR not found: $BASE_JAR" >&2; exit 1; }
BASE_SHA=$(sha256sum "$BASE_JAR" | awk '{print $1}')
if [[ "$BASE_SHA" != "$EXPECTED_BASE_SHA256" && "${ALLOW_UNVERIFIED_BASE:-0}" != "1" ]]; then
  cat >&2 <<ERROR
Base JAR SHA-256 does not match the validated binary.
Expected: $EXPECTED_BASE_SHA256
Actual:   $BASE_SHA
Use the supplied padel-full-1.00.jar, or set ALLOW_UNVERIFIED_BASE=1 for an
explicitly unvalidated experiment.
ERROR
  exit 1
fi

WORK=$(mktemp -d "${TMPDIR:-/tmp}/padel-full-2.00-build.XXXXXX")
trap 'rm -rf "$WORK"' EXIT
mkdir -p "$WORK/classes"

mapfile -d '' SOURCES < <(find "$SCRIPT_DIR/src-relocated" -type f -name '*.java' -print0 | sort -z)
if [[ ${#SOURCES[@]} -eq 0 ]]; then
  echo "No patched sources found." >&2
  exit 1
fi

if javac --help 2>&1 | grep -q -- '--release'; then
  javac --release 8 -cp "$BASE_JAR" -d "$WORK/classes" "${SOURCES[@]}"
else
  javac -source 8 -target 8 -cp "$BASE_JAR" -d "$WORK/classes" "${SOURCES[@]}"
fi

mkdir -p "$(dirname "$OUTPUT_JAR")"
cp "$BASE_JAR" "$OUTPUT_JAR"
jar uf "$OUTPUT_JAR" -C "$WORK/classes" .
jar uf "$OUTPUT_JAR" -C "$SCRIPT_DIR/overlay" .
jar ufm "$OUTPUT_JAR" "$SCRIPT_DIR/manifest-add.mf"
unzip -tqq "$OUTPUT_JAR"

echo "Built: $OUTPUT_JAR"
sha256sum "$OUTPUT_JAR"
