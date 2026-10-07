#!/usr/bin/env python3
"""Create a deterministic executable JAR from a staging directory."""
from __future__ import annotations

import argparse
from pathlib import Path
import zipfile

FIXED_DATE = (2000, 1, 1, 0, 0, 0)


def write_entry(zf: zipfile.ZipFile, name: str, data: bytes) -> None:
    info = zipfile.ZipInfo(name, FIXED_DATE)
    info.compress_type = zipfile.ZIP_DEFLATED
    info.create_system = 3
    info.external_attr = 0o100644 << 16
    zf.writestr(info, data, compress_type=zipfile.ZIP_DEFLATED, compresslevel=9)


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--stage", required=True, type=Path)
    parser.add_argument("--manifest", required=True, type=Path)
    parser.add_argument("--output", required=True, type=Path)
    args = parser.parse_args()

    stage = args.stage.resolve()
    if not stage.is_dir():
        raise SystemExit(f"staging directory does not exist: {stage}")
    manifest = args.manifest.read_bytes()
    if not manifest.endswith(b"\r\n\r\n"):
        raise SystemExit("manifest must end with a blank CRLF line")

    entries = []
    for path in stage.rglob("*"):
        if not path.is_file():
            continue
        rel = path.relative_to(stage).as_posix()
        if rel.upper() == "META-INF/MANIFEST.MF":
            continue
        entries.append((rel, path))
    entries.sort(key=lambda item: item[0])

    args.output.parent.mkdir(parents=True, exist_ok=True)
    temp = args.output.with_suffix(args.output.suffix + ".tmp")
    temp.unlink(missing_ok=True)
    with zipfile.ZipFile(temp, "w", allowZip64=True) as zf:
        write_entry(zf, "META-INF/MANIFEST.MF", manifest)
        for rel, path in entries:
            write_entry(zf, rel, path.read_bytes())
    temp.replace(args.output)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
