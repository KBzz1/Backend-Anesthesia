#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="/home/kbzz1/20260131/backend"
BASE_COMPOSE="${ROOT_DIR}/docker-compose.anesthesia.yml"
HOT_COMPOSE="${ROOT_DIR}/docker-compose.anesthesia.hot.yml"
HOT_SERVICES=(
  anesthesia-postgres
  anesthesia-redis
  anesthesia-mqtt
  anesthesia-base
  anesthesia-app
  anesthesia-gateway
)

cd "${ROOT_DIR}"

if [[ -z "${ANESTHESIA_CLOUDFLARED_TOKEN:-}" ]]; then
  export ANESTHESIA_CLOUDFLARED_TOKEN="disabled-for-hot-reload"
fi

echo "[1/4] Validate compose files"
docker compose -f "${BASE_COMPOSE}" -f "${HOT_COMPOSE}" config >/dev/null

echo "[2/4] Build hot-reload backend images"
docker compose -f "${BASE_COMPOSE}" -f "${HOT_COMPOSE}" build anesthesia-base anesthesia-app

echo "[3/4] Start anesthesia stack (hot reload mode)"
docker compose -f "${BASE_COMPOSE}" -f "${HOT_COMPOSE}" up -d "${HOT_SERVICES[@]}"

echo "[4/4] Service status"
docker compose -f "${BASE_COMPOSE}" -f "${HOT_COMPOSE}" ps "${HOT_SERVICES[@]}"

echo "Anesthesia stack started in hot reload mode without anesthesia-cloudflared."
