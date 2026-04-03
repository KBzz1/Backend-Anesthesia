#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=scripts/tests/lib.sh
source "${SCRIPT_DIR}/lib.sh"

rm -f "${RESULTS_NDJSON}" "${REPORT_DIR}/test-report.json" "${REPORT_DIR}/test-report.md"

echo "[1/4] Run HTTP suite"
"${SCRIPT_DIR}/http_suite.sh"

echo "[2/4] Run WebSocket suite"
"${SCRIPT_DIR}/ws_suite.sh"

echo "[3/4] Build report artifacts"
export REPORT_DIR RESULTS_NDJSON
run_py "${SCRIPT_DIR}/report_builder.py"

echo "[4/4] Done"
echo "Report JSON: ${REPORT_DIR}/test-report.json"
echo "Report MD:   ${REPORT_DIR}/test-report.md"
