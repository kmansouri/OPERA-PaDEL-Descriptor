#!/usr/bin/env python3
"""Validate PaDEL descriptor CSV row widths and optional completeness."""
from __future__ import annotations

import argparse
import csv
from pathlib import Path
import sys


def main() -> int:
    p = argparse.ArgumentParser()
    p.add_argument("csv", type=Path)
    p.add_argument("--rows", type=int, help="expected data-row count")
    p.add_argument("--descriptors", type=int, help="expected descriptor count, excluding Name")
    p.add_argument("--allow-missing", action="store_true")
    p.add_argument("--missing-token", default="")
    args = p.parse_args()

    with args.csv.open(newline="", encoding="utf-8") as handle:
        rows = list(csv.reader(handle))
    if not rows:
        print("ERROR: empty CSV", file=sys.stderr)
        return 2
    width = len(rows[0])
    if width < 2 or rows[0][0] != "Name":
        print("ERROR: invalid PaDEL header", file=sys.stderr)
        return 2
    if args.descriptors is not None and width != args.descriptors + 1:
        print(f"ERROR: expected {args.descriptors + 1} columns, found {width}", file=sys.stderr)
        return 2
    if args.rows is not None and len(rows) - 1 != args.rows:
        print(f"ERROR: expected {args.rows} rows, found {len(rows) - 1}", file=sys.stderr)
        return 2

    bad_widths = [(idx, len(row)) for idx, row in enumerate(rows[1:], 2) if len(row) != width]
    if bad_widths:
        print(f"ERROR: inconsistent row widths: {bad_widths[:10]}", file=sys.stderr)
        return 2

    missing = []
    for r_idx, row in enumerate(rows[1:], 2):
        for c_idx, value in enumerate(row[1:], 2):
            if value == args.missing_token:
                missing.append((r_idx, c_idx, rows[0][c_idx - 1]))
    if missing and not args.allow_missing:
        print(f"ERROR: {len(missing)} missing values; first={missing[:10]}", file=sys.stderr)
        return 2

    print(
        f"OK rows={len(rows)-1} descriptors={width-1} "
        f"missing={len(missing)} file={args.csv}"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
