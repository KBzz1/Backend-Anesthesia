#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${1:-http://127.0.0.1:18080}"

echo "[1/6] GET /areas/online"
curl --noproxy '*' -fsS "${BASE_URL}/areas/online"
echo

echo "[2/6] GET /device/binding/statistics"
curl --noproxy '*' -fsS "${BASE_URL}/device/binding/statistics"
echo

echo "[3/6] GET /queue/check/0"
curl --noproxy '*' -fsS "${BASE_URL}/queue/check/0"
echo

echo "[4/6] GET /patients/status/0"
curl --noproxy '*' -fsS "${BASE_URL}/patients/status/0"
echo

echo "[5/6] GET /waveform/0"
curl --noproxy '*' -fsS "${BASE_URL}/waveform/0"
echo

echo "[6/6] GET /patients/0"
curl --noproxy '*' -fsS "${BASE_URL}/patients/0"
echo
