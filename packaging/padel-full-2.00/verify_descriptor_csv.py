#!/usr/bin/env python3
"""Report blank and non-finite PaDEL descriptor fields by molecule."""

from __future__ import annotations

import argparse
import csv
import math
from pathlib import Path


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("csv_file", type=Path)
    args = parser.parse_args()

    with args.csv_file.open(newline="", encoding="utf-8-sig") as handle:
        reader = csv.reader(handle)
        try:
            header = next(reader)
        except StopIteration:
            raise SystemExit("CSV is empty")

        if len(header) < 2:
            raise SystemExit("CSV has no descriptor columns")

        total_bad = 0
        rows = 0
        for line_number, row in enumerate(reader, start=2):
            rows += 1
            if len(row) != len(header):
                print(f"line {line_number}: {len(row)} fields; expected {len(header)}")
                total_bad += abs(len(header) - len(row)) or 1
                continue

            bad = []
            for index, value in enumerate(row[1:], start=1):
                text = value.strip()
                if not text:
                    bad.append(header[index])
                    continue
                try:
                    number = float(text)
                except ValueError:
                    continue
                if not math.isfinite(number):
                    bad.append(header[index])

            if bad:
                total_bad += len(bad)
                name = row[0] or f"line {line_number}"
                preview = ", ".join(bad[:12])
                suffix = " ..." if len(bad) > 12 else ""
                print(f"{name}: {len(bad)} blank/non-finite fields: {preview}{suffix}")

    print(f"rows={rows}, descriptor_columns={len(header)-1}, bad_fields={total_bad}")
    return 1 if total_bad else 0


if __name__ == "__main__":
    raise SystemExit(main())
