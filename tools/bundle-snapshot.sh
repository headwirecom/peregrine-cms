#!/usr/bin/env bash
# ---------------------------------------------------------------------------
# bundle-snapshot.sh  –  copy all built OSGi bundle JARs into a snapshot dir.
#
# Usage:
#   ./tools/bundle-snapshot.sh [SNAPSHOT_DIR]
#
# SNAPSHOT_DIR defaults to  snapshots/<git-branch>_<timestamp>
#
# The snapshot mirrors the project tree:
#   <SNAPSHOT_DIR>/
#     <module-relative-path>/
#       <artifact>.jar
#     INDEX.txt             – list of captured bundles + branch/date
#
# After: switch branches, rebuild, run again, then use bundle-diff.sh to compare.
# ---------------------------------------------------------------------------
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BRANCH=$(git -C "$REPO_ROOT" rev-parse --abbrev-ref HEAD 2>/dev/null \
         | tr '/' '_' \
         || echo "unknown")
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
SNAPSHOT_DIR="${1:-$REPO_ROOT/snapshots/${BRANCH}_${TIMESTAMP}}"

echo "Snapshot dir : $SNAPSHOT_DIR"
echo "Branch       : $BRANCH"
echo "Scanning     : $REPO_ROOT"
echo ""

FOUND=0

while IFS= read -r jar; do
    name=$(basename "$jar")
    # skip classifiers we don't care about
    case "$name" in
        *-sources.jar|*-tests.jar|*-test.jar|*-javadoc.jar) continue ;;
    esac

    # quick check: is this an OSGi bundle? (must have Bundle-SymbolicName)
    # use unzip -p to read the manifest without extracting
    manifest=$(unzip -p "$jar" META-INF/MANIFEST.MF 2>/dev/null) || continue
    echo "$manifest" | grep -q "^Bundle-SymbolicName" || continue

    # module-relative path = strip REPO_ROOT prefix and /target/xxx.jar suffix
    rel="${jar#"$REPO_ROOT"/}"
    module_dir="${rel%/target/*}"

    dest="$SNAPSHOT_DIR/$module_dir"
    mkdir -p "$dest"
    cp "$jar" "$dest/$name"

    echo "  ✓  $module_dir/$name"
    FOUND=$((FOUND + 1))
done < <(find "$REPO_ROOT" \
    -path "*/target/*.jar" \
    ! -path "*/target/dependency/*" \
    ! -path "*/node_modules/*" \
    2>/dev/null | sort)

if [[ $FOUND -eq 0 ]]; then
    echo "No OSGi bundles found — run 'mvn package' first."
    rmdir "$SNAPSHOT_DIR" 2>/dev/null || true
    exit 1
fi

# index file
{
    printf "Branch    : %s\n" "$BRANCH"
    printf "Date      : %s\n" "$(date -Iseconds)"
    printf "Bundles   : %d\n" "$FOUND"
    printf "\n"
    find "$SNAPSHOT_DIR" -name "*.jar" | sort | sed "s|$SNAPSHOT_DIR/||"
} > "$SNAPSHOT_DIR/INDEX.txt"

echo ""
echo "Captured $FOUND bundle(s) → $SNAPSHOT_DIR"
