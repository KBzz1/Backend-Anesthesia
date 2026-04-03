#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=scripts/tests/lib.sh
source "${SCRIPT_DIR}/lib.sh"

run_py "${SCRIPT_DIR}/http_suite.py"

