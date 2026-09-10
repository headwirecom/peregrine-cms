#!/usr/bin/env python3
"""
bundle-diff.py  –  compare two OSGi bundle snapshots.

Uses `bnd diff` for the structural diff and adds clause-level normalised
diffing for headers bnd treats as opaque strings.

Usage:
    python3 bundle-diff.py [options] <baseline-dir> <candidate-dir>

Options:
    --bnd JAR           path to biz.aQute.bnd-*.jar
                        (default: $HOME/.local/share/bnd/biz.aQute.bnd-7.4.0.jar
                         or BND_JAR env var)
    --raw               show full unfiltered bnd diff output; disable all
                        default filters
    --show-resources    include MAJOR RESOURCE / SHA change blocks (noisy;
                        default: suppressed)
    --show-java-imports include  REMOVED/ADDED CLAUSE java.*  lines
                        (default: suppressed)
    --show-unchanged    include UNCHANGED lines from bnd diff output
                        (default: suppressed)

Exit codes:
    0  no semantic differences
    1  differences found (or bundles added/removed)
    2  usage error / missing bnd jar
"""

import argparse
import difflib
import os
import re
import shutil
import subprocess
import sys
import zipfile
from pathlib import Path


# ---------------------------------------------------------------------------
# Constants
# ---------------------------------------------------------------------------

BND_URL = (
    "https://repo1.maven.org/maven2/biz/aQute/bnd/biz.aQute.bnd"
    "/7.4.0/biz.aQute.bnd-7.4.0.jar"
)

# Headers we post-process with clause-level normalisation instead of relying
# on bnd diff (bnd treats these as opaque strings).
STRUCTURED_HEADERS = [
    "Require-Capability",
    "Provide-Capability",
    "Embedded-Artifacts",
    "Bundle-ClassPath",
]

HR  = "═" * 56
HR2 = "─" * 56


# ---------------------------------------------------------------------------
# Manifest parsing
# ---------------------------------------------------------------------------

def read_manifest(jar_path: Path) -> dict[str, str]:
    """Return the MANIFEST.MF as a dict of header → value (continuation-unfolded)."""
    try:
        with zipfile.ZipFile(jar_path) as z:
            raw = z.read("META-INF/MANIFEST.MF").decode("utf-8", errors="replace")
    except (KeyError, zipfile.BadZipFile):
        return {}

    headers: dict[str, str] = {}
    current_key = None
    current_val: list[str] = []

    for line in raw.splitlines():
        line = line.rstrip("\r")
        if line.startswith(" "):                    # continuation
            if current_key:
                current_val.append(line[1:])
        else:
            if current_key:
                headers[current_key] = "".join(current_val)
            if ":" in line:
                k, _, v = line.partition(":")
                current_key = k.strip()
                current_val = [v.lstrip(" ")]
            else:
                current_key = None

    if current_key:
        headers[current_key] = "".join(current_val)

    return headers


# ---------------------------------------------------------------------------
# Clause / parameter splitting  (quote-aware)
# ---------------------------------------------------------------------------

def split_quoted(text: str, delimiter: str) -> list[str]:
    """Split *text* on *delimiter* but not when inside double-quoted strings."""
    parts: list[str] = []
    current: list[str] = []
    in_q = False
    for ch in text:
        if ch == '"':
            in_q = not in_q
            current.append(ch)
        elif ch == delimiter and not in_q:
            parts.append("".join(current).strip())
            current = []
        else:
            current.append(ch)
    tail = "".join(current).strip()
    if tail:
        parts.append(tail)
    return [p for p in parts if p]


def normalise_header(value: str) -> list[str]:
    """
    Normalise an OSGi structured header value to a sorted list of clauses,
    where the parameters within each clause are also sorted (keeping the
    first token — namespace or filename — in position).
    """
    clauses = split_quoted(value, ",")
    result: list[str] = []
    for clause in clauses:
        params = split_quoted(clause, ";")
        if not params:
            continue
        head = params[0]
        rest = sorted(params[1:])
        result.append(";".join([head] + rest))
    return sorted(result)


# ---------------------------------------------------------------------------
# bnd invocation
# ---------------------------------------------------------------------------

def run_bnd_diff(bnd_jar: Path, newer: Path, older: Path) -> str:
    """Run `bnd diff <newer> <older>` and return combined stdout+stderr."""
    result = subprocess.run(
        ["java", "-jar", str(bnd_jar), "diff", str(newer), str(older)],
        capture_output=True,
        text=True,
    )
    return result.stdout + result.stderr


# ---------------------------------------------------------------------------
# bnd output filtering
# ---------------------------------------------------------------------------

# Block pattern: a MAJOR RESOURCE line followed immediately by exactly two
# REMOVED/ADDED SHA lines.  These are class-file or resource binary changes
# that are verbose and usually uninteresting.
_RESOURCE_BLOCK = re.compile(
    r"^\s*MAJOR\s+RESOURCE\s+\S+\n"
    r"\s*(?:REMOVED|ADDED)\s+SHA\s+\S+\n"
    r"\s*(?:REMOVED|ADDED)\s+SHA\s+\S+\n",
    re.MULTILINE,
)

# Single-line patterns to suppress
_JAVA_IMPORT = re.compile(r"^\s*(?:REMOVED|ADDED)\s+CLAUSE\s+java\.", re.MULTILINE)

# bnd emits  REMOVED/ADDED  HEADER  <HeaderName>:<full-value>  for headers it
# treats as opaque strings.  We handle these ourselves with clause-level diffs,
# so suppress bnd's noisy single-line version for each of them.
_OPAQUE_HEADER_RE = re.compile(
    r"^\s*(?:REMOVED|ADDED|CHANGED)\s+HEADER\s+"
    r"(?:" + "|".join(re.escape(h) for h in STRUCTURED_HEADERS) + r")(?::|$|\s)",
    re.MULTILINE,
)


def filter_bnd_output(raw: str, args: argparse.Namespace) -> str:
    """Apply default (and optionally disabled) filters to raw bnd diff output."""
    if args.raw:
        return raw

    text = raw

    if not args.show_unchanged:
        text = "\n".join(
            line for line in text.splitlines()
            if not re.match(r"^\s*UNCHANGED", line)
        )

    if not args.show_resources:
        text = _RESOURCE_BLOCK.sub("", text)

    if not args.show_java_imports:
        text = "\n".join(
            line for line in text.splitlines()
            if not _JAVA_IMPORT.match(line)
        )

    # suppress full-value opaque-header lines for headers we diff ourselves
    text = "\n".join(
        line for line in text.splitlines()
        if not _OPAQUE_HEADER_RE.match(line)
    )

    # collapse runs of blank lines to a single blank
    text = re.sub(r"\n{3,}", "\n\n", text)
    return text.strip()


# ---------------------------------------------------------------------------
# Structured-header diff
# ---------------------------------------------------------------------------

def diff_structured_header(
    header: str, base_jar: Path, cand_jar: Path
) -> list[str] | None:
    """
    Return a list of diff lines for *header* between the two JARs, or None
    if the header is identical in both (after normalisation).
    """
    base_mf = read_manifest(base_jar)
    cand_mf = read_manifest(cand_jar)

    base_val = base_mf.get(header, "")
    cand_val = cand_mf.get(header, "")

    base_clauses = normalise_header(base_val) if base_val else []
    cand_clauses = normalise_header(cand_val) if cand_val else []

    if base_clauses == cand_clauses:
        return None

    lines: list[str] = [f"  ── {header} clauses"]
    base_set = set(base_clauses)
    cand_set = set(cand_clauses)

    for c in sorted(base_set - cand_set):
        lines.append(f"  REMOVED  CLAUSE  {c}")
    for c in sorted(cand_set - base_set):
        lines.append(f"  ADDED    CLAUSE  {c}")

    return lines


# ---------------------------------------------------------------------------
# Bundle discovery
# ---------------------------------------------------------------------------

def find_bundles(snapshot_dir: Path) -> dict[str, Path]:
    """
    Return {module_dir: jar_path} for every OSGi bundle JAR under snapshot_dir.
    Module dir is the relative path of the directory containing the JAR.
    """
    bundles: dict[str, Path] = {}
    for jar in sorted(snapshot_dir.rglob("*.jar")):
        name = jar.name
        if any(name.endswith(s) for s in ("-sources.jar", "-tests.jar",
                                           "-test.jar", "-javadoc.jar")):
            continue
        mf = read_manifest(jar)
        if "Bundle-SymbolicName" not in mf:
            continue
        module = str(jar.parent.relative_to(snapshot_dir))
        bundles[module] = jar
    return bundles


# ---------------------------------------------------------------------------
# Ensure bnd jar
# ---------------------------------------------------------------------------

def ensure_bnd(bnd_jar: Path) -> None:
    if bnd_jar.exists():
        return
    print(f"bnd jar not found at {bnd_jar}")
    print("Downloading from Maven Central…")
    bnd_jar.parent.mkdir(parents=True, exist_ok=True)
    if shutil.which("curl"):
        subprocess.run(["curl", "-fL", BND_URL, "-o", str(bnd_jar)], check=True)
    elif shutil.which("wget"):
        subprocess.run(["wget", "-q", BND_URL, "-O", str(bnd_jar)], check=True)
    else:
        sys.exit(
            f"ERROR: neither curl nor wget found.\n"
            f"Download manually:\n  {BND_URL}\n  → {bnd_jar}"
        )
    print(f"Downloaded: {bnd_jar}")


# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------

def main() -> int:
    parser = argparse.ArgumentParser(
        description="Compare two OSGi bundle snapshots.",
        formatter_class=argparse.RawDescriptionHelpFormatter,
    )
    parser.add_argument("baseline", type=Path, help="Baseline snapshot directory")
    parser.add_argument("candidate", type=Path, help="Candidate snapshot directory")
    parser.add_argument(
        "--bnd",
        type=Path,
        default=Path(
            os.environ.get(
                "BND_JAR",
                Path.home() / ".local/share/bnd/biz.aQute.bnd-7.4.0.jar",
            )
        ),
        metavar="JAR",
        help="Path to biz.aQute.bnd-*.jar",
    )
    parser.add_argument(
        "--raw",
        action="store_true",
        help="Disable all filters; show full bnd diff output",
    )
    parser.add_argument(
        "--show-resources",
        action="store_true",
        help="Show MAJOR RESOURCE / SHA change blocks (default: hidden)",
    )
    parser.add_argument(
        "--show-java-imports",
        action="store_true",
        help="Show REMOVED/ADDED CLAUSE java.* lines (default: hidden)",
    )
    parser.add_argument(
        "--show-unchanged",
        action="store_true",
        help="Show UNCHANGED lines from bnd diff (default: hidden)",
    )

    args = parser.parse_args()

    if not args.baseline.is_dir():
        sys.exit(f"ERROR: baseline not found: {args.baseline}")
    if not args.candidate.is_dir():
        sys.exit(f"ERROR: candidate not found: {args.candidate}")

    ensure_bnd(args.bnd)

    bnd_ver = subprocess.run(
        ["java", "-jar", str(args.bnd), "version"],
        capture_output=True, text=True,
    ).stdout.strip()

    base_bundles = find_bundles(args.baseline)
    cand_bundles = find_bundles(args.candidate)

    base_modules = set(base_bundles)
    cand_modules = set(cand_bundles)

    print()
    print(f"  Baseline  : {args.baseline}")
    print(f"  Candidate : {args.candidate}")
    print(f"  bnd       : {bnd_ver}")
    print(HR)

    total_diffs = 0
    n_added = n_removed = n_changed = 0

    # Removed bundles
    for m in sorted(base_modules - cand_modules):
        print(f"  REMOVED  {m}")
        n_removed += 1
        total_diffs += 1

    # Added bundles
    for m in sorted(cand_modules - base_modules):
        print(f"  ADDED    {m}")
        n_added += 1
        total_diffs += 1

    # Compare common bundles
    for module in sorted(base_modules & cand_modules):
        base_jar = base_bundles[module]
        cand_jar = cand_bundles[module]

        print()
        print(HR2)
        print(f"\n  MODULE: {module}")
        print(f"    baseline  : {base_jar.name}")
        print(f"    candidate : {cand_jar.name}")
        print()

        module_changed = False

        # 1. bnd diff (handles Export/Import-Package, DS components, etc.)
        raw_bnd = run_bnd_diff(args.bnd, cand_jar, base_jar)
        filtered_bnd = filter_bnd_output(raw_bnd, args)
        if filtered_bnd:
            print(filtered_bnd)
            module_changed = True

        # 2. Structured headers bnd treats as opaque
        for header in STRUCTURED_HEADERS:
            diff_lines = diff_structured_header(header, base_jar, cand_jar)
            if diff_lines is not None:
                print("\n".join(diff_lines))
                module_changed = True

        if not module_changed:
            print("  ✓  No differences")
        else:
            n_changed += 1
            total_diffs += 1

    # Summary
    print()
    print(HR)
    print()
    print(f"  Bundles added    : {n_added}")
    print(f"  Bundles removed  : {n_removed}")
    print(f"  Bundles changed  : {n_changed}")
    print(f"  Total with diffs : {total_diffs}")
    print()

    if total_diffs == 0:
        print("  ✓  Snapshots are semantically identical.")
        return 0
    else:
        print("  ✗  Differences found.")
        return 1


if __name__ == "__main__":
    sys.exit(main())
