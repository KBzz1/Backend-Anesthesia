#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

if command -v python3 >/dev/null 2>&1; then
  exec python3 "${SCRIPT_DIR}/cloudflare_api_probe.py" "$@"
fi

# shellcheck source=scripts/tests/lib.sh
source "${SCRIPT_DIR}/lib.sh"
run_py "${SCRIPT_DIR}/cloudflare_api_probe.py" "$@"
