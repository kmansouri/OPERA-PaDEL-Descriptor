#!/usr/bin/env bash
set -euo pipefail
ROOT=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
rm -rf "$ROOT/build" "$ROOT/padel-full-3.00-cdk2.jar"
