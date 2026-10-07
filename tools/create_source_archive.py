#!/usr/bin/env python3
"""Create a deterministic self-contained source archive for this release."""
from __future__ import annotations

import argparse
import os
from pathlib import Path, PurePosixPath
import stat
import zipfile

FIXED_TIME = (1980, 1, 1, 0, 0, 0)
EXCLUDED_DIRS = {"build", ".git", "__pycache__"}
EXCLUDED_FILES = {"padel-full-3.00-cdk2.jar"}


def included(path: Path, root: Path) -> bool:
    rel = path.relative_to(root)
    if any(part in EXCLUDED_DIRS for part in rel.parts):
        return False
    if rel.name in EXCLUDED_FILES or rel.suffix == ".pyc":
        return False
    return path.is_file()


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", required=True, type=Path)
    parser.add_argument("--output", required=True, type=Path)
    parser.add_argument("--prefix", default="padel-full-3.00-cdk2-source")
    args = parser.parse_args()
    root = args.root.resolve()
    output = args.output.resolve()
    output.parent.mkdir(parents=True, exist_ok=True)
    temporary = output.with_suffix(output.suffix + ".tmp")
    if temporary.exists():
        temporary.unlink()

    paths = sorted((p for p in root.rglob("*") if included(p, root)),
                   key=lambda p: p.relative_to(root).as_posix())
    with zipfile.ZipFile(temporary, "w", compression=zipfile.ZIP_DEFLATED,
                         compresslevel=9, allowZip64=True) as archive:
        for path in paths:
            rel = PurePosixPath(args.prefix) / PurePosixPath(path.relative_to(root).as_posix())
            info = zipfile.ZipInfo(str(rel), FIXED_TIME)
            mode = path.stat().st_mode
            permissions = 0o755 if mode & stat.S_IXUSR else 0o644
            info.external_attr = (stat.S_IFREG | permissions) << 16
            info.compress_type = zipfile.ZIP_DEFLATED
            info.create_system = 3
            with path.open("rb") as handle:
                archive.writestr(info, handle.read(), compress_type=zipfile.ZIP_DEFLATED,
                                 compresslevel=9)
    os.replace(temporary, output)
    print("Created %s with %d files" % (output, len(paths)))


if __name__ == "__main__":
    main()
