#!/usr/bin/env bash
# Resolve crash addresses against a published build without re-running anything in CI.
#   .ci/android/symbolize.sh <build-id | run-id> 0xADDR [0xADDR...]
# Fetches the symbol-maps branch, unpacks the matching unstripped library and runs
# llvm-symbolizer with inlined frames and source lines.
set -euo pipefail
key=$1; shift
cache=${SYMBOL_CACHE:-$HOME/.cache/eden-symbols}
mkdir -p "$cache"
if [ ! -d "$cache/.git" ]; then
    git -C "$cache" init -q
    git -C "$cache" remote add origin "$(git remote get-url origin)"
fi
git -C "$cache" fetch -q --depth 1 origin symbol-maps
git -C "$cache" checkout -q -f FETCH_HEAD
lib=$(ls "$cache"/libs/*"$key"*.so.xz 2>/dev/null | head -1 || true)
if [ -z "$lib" ]; then
    echo "No library for '$key'. Available:" >&2
    ls "$cache"/libs >&2
    exit 1
fi
so="${lib%.xz}"
[ -f "$so" ] || xz -dk "$lib"
for a in "$@"; do
    echo "== $a"
    llvm-symbolizer --obj="$so" --inlines --functions=linkage -C "$a"
done
