#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="/home/kbzz1/20260131/backend"
REPORT_DIR="${ROOT_DIR}/reports"
RESULTS_NDJSON="${REPORT_DIR}/test-results.ndjson"

BASE_URL="${BASE_URL:-https://127.0.0.1:8080}"
AUTH_EMAIL="${AUTH_EMAIL:-zhaomin@hospital.com}"
AUTH_PASSWORD="${AUTH_PASSWORD:-zhaomin123}"

PY_BIN="${PY_BIN:-}"
if [[ -z "${PY_BIN}" ]]; then
  if command -v conda >/dev/null 2>&1; then
    PY_BIN="conda run -n anesthesia python"
  else
    PY_BIN="python3"
  fi
fi

mkdir -p "${REPORT_DIR}"

run_py() {
  # shellcheck disable=SC2086
  eval ${PY_BIN} "$@"
}

