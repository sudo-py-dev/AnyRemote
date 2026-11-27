#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
formatter="${KTFMT_JAR:-$repo_root/ktfmt.jar}"
options=(--kotlinlang-style)

case "${1:-}" in
    '') ;;
    --check) options+=(--dry-run --set-exit-if-changed) ;;
    *) printf 'Usage: %s [--check]\n' "$0" >&2; exit 2 ;;
esac

if [[ ! -f "$formatter" ]]; then
    printf 'ktfmt not found. Set KTFMT_JAR or place ktfmt.jar in the repository root.\n' >&2
    exit 1
fi

sources=("$repo_root/build.gradle.kts" "$repo_root/settings.gradle.kts")
while IFS= read -r -d '' source; do
    sources+=("$source")
done < <(find "$repo_root/src" -type f -name '*.kt' -print0)

exec java -jar "$formatter" "${options[@]}" "${sources[@]}"
