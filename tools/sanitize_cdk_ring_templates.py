#!/usr/bin/env python3
"""Remove invalid negative M  ISO records from CDK 2.13 ring templates.

CDK's bundled ring-template SDF contains 102 records such as
"M  ISO  1   4  -1". V2000 M  ISO records are absolute mass numbers, so CDK
correctly rejects negative values and logs one ERROR per line while otherwise
ignoring them. Removing these invalid records is therefore semantically
consistent with the reader's behavior and prevents noisy stderr output during
first use of ModelBuilder3D.
"""
from __future__ import print_function

import argparse
import gzip
import io
import os
import re
import tempfile

NEGATIVE_ISO = re.compile(br"^M  ISO(?:\s+\d+)(?:\s+\d+\s+-\d+)+\s*(?:\r?\n)?$")


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("path", help="Path to ringTemplateStructures.sdf.gz")
    parser.add_argument("--expected", type=int, default=102,
                        help="Expected number of invalid lines (default: 102)")
    args = parser.parse_args()

    with gzip.open(args.path, "rb") as handle:
        original = handle.read()
    lines = original.splitlines(True)
    kept = []
    removed = []
    for line in lines:
        if NEGATIVE_ISO.match(line):
            removed.append(line.rstrip(b"\r\n"))
        else:
            kept.append(line)

    if len(removed) != args.expected:
        raise SystemExit("expected %d invalid negative M  ISO records, found %d"
                         % (args.expected, len(removed)))

    directory = os.path.dirname(os.path.abspath(args.path))
    fd, temporary = tempfile.mkstemp(prefix="ring-templates-", suffix=".gz",
                                     dir=directory)
    os.close(fd)
    try:
        with open(temporary, "wb") as raw:
            with gzip.GzipFile(filename="", mode="wb", fileobj=raw, mtime=0) as out:
                out.write(b"".join(kept))
        os.replace(temporary, args.path)
    finally:
        if os.path.exists(temporary):
            os.unlink(temporary)

    print("Sanitized CDK ring templates: removed %d invalid negative M  ISO records"
          % len(removed))


if __name__ == "__main__":
    main()
