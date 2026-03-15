#!/bin/bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

fail=0

assert_absent() {
    local pattern="$1"
    shift
    local paths=("$@")

    if rg -n "$pattern" "${paths[@]}" >/dev/null 2>&1; then
        echo "[FAIL] Found forbidden pattern: $pattern"
        rg -n "$pattern" "${paths[@]}" || true
        fail=1
    else
        echo "[OK] Pattern absent: $pattern"
    fi
}

assert_absent "CopyOnWriteArrayList" src
assert_absent "TimerTask|java\\.util\\.Timer|new Timer\\(" src/main src/utils src/entity
assert_absent "\\.stream\\(" src/main/Game.java src/utils/Renderer.java src/utils/WindowRenderer.java src/utils/RoomWindow.java
assert_absent "new Thread\\(" src/entity/Gun.java src/main/Game.java

if [ "$fail" -ne 0 ]; then
    exit 1
fi

echo "[PASS] Hot-path regression checks passed."
