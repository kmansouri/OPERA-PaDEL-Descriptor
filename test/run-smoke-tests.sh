#!/usr/bin/env bash
set -euo pipefail
ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
JAR="$ROOT/padel-full-3.00-cdk2.jar"
VERIFY="$ROOT/tools/verify_descriptor_csv.py"
TMP=$(mktemp -d "${TMPDIR:-/tmp}/padel3-test.XXXXXX")
trap 'rm -rf "$TMP"' EXIT

if [[ ! -f "$JAR" ]]; then
    "$ROOT/build.sh"
fi

java -jar "$JAR" -version | grep -F 'PaDEL-Descriptor CDK2 3.00-cdk2 (CDK 2.13)'

java -jar "$JAR" -2d -listschema "$TMP/schema.csv" -quiet
[[ $(wc -l < "$TMP/schema.csv") -eq 1445 ]]

COMMON=(
    -2d -removesalt -standardizenitro -detectaromaticity
    -retainorder -maxruntime 60000 -quiet
    -dir "$ROOT/test/fixtures/test_smi.smi"
)
java -jar "$JAR" "${COMMON[@]}" -threads 1 -file "$TMP/t1.csv"
java -jar "$JAR" "${COMMON[@]}" -threads 8 -file "$TMP/t8.csv"
cmp "$TMP/t1.csv" "$TMP/t8.csv"
python3 "$VERIFY" "$TMP/t1.csv" --rows 3 --descriptors 1444

# Malformed input is retained as a missing row. Default mode completes; strict
# mode makes the molecule-level problem visible to orchestration.
java -jar "$JAR" -2d -retainorder -threads 2 -maxruntime 60000 -quiet \
    -dir "$ROOT/test/fixtures/mixed-invalid.smi" -file "$TMP/mixed.csv"
python3 "$VERIFY" "$TMP/mixed.csv" --rows 3 --descriptors 1444 --allow-missing
set +e
java -jar "$JAR" -2d -retainorder -threads 2 -maxruntime 60000 -strict -quiet \
    -dir "$ROOT/test/fixtures/mixed-invalid.smi" -file "$TMP/mixed-strict.csv"
status=$?
set -e
[[ $status -eq 4 ]]

# Timeout path and strict exit code.
set +e
java -jar "$JAR" -2d -retainorder -threads 2 -maxruntime 1 -strict -quiet \
    -dir "$ROOT/test/fixtures/test_smi.smi" -file "$TMP/timeout.csv"
status=$?
set -e
[[ $status -eq 4 ]]
python3 "$VERIFY" "$TMP/timeout.csv" --rows 3 --descriptors 1444 --allow-missing
python3 - "$TMP/timeout.csv.metadata.json" <<'PY'
import json, sys
m = json.load(open(sys.argv[1], encoding='utf-8'))
assert m['timedOutMolecules'] >= 1, m
PY

# Default fingerprint and 3-D profiles.
java -jar "$JAR" -fingerprints -retainorder -threads 2 -maxruntime 60000 -quiet \
    -dir "$ROOT/test/fixtures/test_smi.smi" -file "$TMP/fp.csv"
python3 "$VERIFY" "$TMP/fp.csv" --rows 3 --descriptors 881

java -jar "$JAR" -3d -retain3d -retainorder -threads 1 -maxruntime 60000 -quiet \
    -dir "$ROOT/test/fixtures/ethane-3d.mol" -file "$TMP/3d.csv"
python3 "$VERIFY" "$TMP/3d.csv" --rows 1 --descriptors 431

# Optional larger deterministic stress test.
if [[ ${FULL:-0} == 1 ]]; then
    for t in 1 8; do
        java -jar "$JAR" -2d -removesalt -standardizenitro -detectaromaticity \
            -retainorder -threads "$t" -maxruntime 60000 -quiet \
            -dir "$ROOT/test/fixtures/stress-120.smi" -file "$TMP/stress-t${t}.csv"
        python3 "$VERIFY" "$TMP/stress-t${t}.csv" --rows 120 \
            --descriptors 1444 --allow-missing
    done
    cmp "$TMP/stress-t1.csv" "$TMP/stress-t8.csv"

    # CDK 2.13 caches mutable ModelBuilder3D/template singletons. This
    # regression test verifies PaDEL's serialized coordinate-generation guard
    # and the sanitized ring-template resource under multiple molecule workers.
    for t in 1 4; do
        java -jar "$JAR" -3d -convert3d -retainorder -threads "$t" \
            -maxruntime 120000 -quiet -dir "$ROOT/test/fixtures/test_smi.smi" \
            -file "$TMP/generated-3d-t${t}.csv" \
            >"$TMP/generated-3d-t${t}.stdout" 2>"$TMP/generated-3d-t${t}.stderr"
        python3 "$VERIFY" "$TMP/generated-3d-t${t}.csv" --rows 3 --descriptors 431
        [[ ! -s "$TMP/generated-3d-t${t}.stdout" ]]
        [[ ! -s "$TMP/generated-3d-t${t}.stderr" ]]
    done
    cmp "$TMP/generated-3d-t1.csv" "$TMP/generated-3d-t4.csv"
fi

unzip -tq "$JAR" >/dev/null
echo "All PaDEL-Descriptor CDK2 smoke tests passed."
