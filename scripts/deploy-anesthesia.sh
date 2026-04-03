#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="/home/kbzz1/20260131/backend"
COMPOSE_FILE="${ROOT_DIR}/docker-compose.anesthesia.yml"

cd "${ROOT_DIR}"

echo "[1/4] Validate compose file"
docker compose -f "${COMPOSE_FILE}" config >/dev/null

echo "[2/4] Build backends"
docker compose -f "${COMPOSE_FILE}" build anesthesia-base anesthesia-app

echo "[3/4] Start anesthesia stack"
docker compose -f "${COMPOSE_FILE}" up -d

echo "[4/4] Service status"
docker compose -f "${COMPOSE_FILE}" ps

echo "Anesthesia stack started."
