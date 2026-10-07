#!/usr/bin/env python3
"""Compare two PaDEL CSVs by molecule name and descriptor column."""
from __future__ import annotations

import argparse
import csv
import math
from pathlib import Path


def load(path: Path):
    with path.open(newline="", encoding="utf-8") as handle:
        rows = list(csv.reader(handle))
    if not rows:
        raise ValueError(f"empty CSV: {path}")
    header = rows[0]
    return header, {row[0]: row[1:] for row in rows[1:]}


def number(value: str):
    try:
        return float(value)
    except ValueError:
        return None


def main() -> int:
    p = argparse.ArgumentParser()
    p.add_argument("left", type=Path)
    p.add_argument("right", type=Path)
    p.add_argument("--output", type=Path)
    p.add_argument("--atol", type=float, default=0.0)
    p.add_argument("--rtol", type=float, default=0.0)
    args = p.parse_args()

    hl, left = load(args.left)
    hr, right = load(args.right)
    if hl != hr:
        raise SystemExit("headers differ; compare schemas before comparing values")
    names = sorted(set(left) | set(right))
    differences = []
    for mol in names:
        lv = left.get(mol)
        rv = right.get(mol)
        if lv is None or rv is None:
            differences.append((mol, "<row>", "missing" if lv is None else "present",
                                "missing" if rv is None else "present", ""))
            continue
        for idx, (a, b) in enumerate(zip(lv, rv), 1):
            if a == b:
                continue
            fa, fb = number(a), number(b)
            equal = fa is not None and fb is not None and math.isclose(
                fa, fb, rel_tol=args.rtol, abs_tol=args.atol
            )
            if not equal:
                delta = ""
                if fa is not None and fb is not None:
                    delta = repr(fb - fa)
                differences.append((mol, hl[idx], a, b, delta))

    if args.output:
        args.output.parent.mkdir(parents=True, exist_ok=True)
        with args.output.open("w", newline="", encoding="utf-8") as handle:
            writer = csv.writer(handle)
            writer.writerow(["Name", "Descriptor", "Left", "Right", "RightMinusLeft"])
            writer.writerows(differences)
    print(f"differences={len(differences)} molecules={len(names)} descriptors={len(hl)-1}")
    return 1 if differences else 0


if __name__ == "__main__":
    raise SystemExit(main())
