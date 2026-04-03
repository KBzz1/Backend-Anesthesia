#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
IMAGE_ARCHIVE="${ROOT_DIR}/docker-images/anesthesia-images.tar"
DB_ARCHIVE="${ROOT_DIR}/database/anesthesia.sql.gz"
COMPOSE_FILE="${ROOT_DIR}/docker-compose.anesthesia.yml"

if [[ ! -f "${IMAGE_ARCHIVE}" ]]; then
  echo "Missing Docker image archive: ${IMAGE_ARCHIVE}" >&2
  exit 1
fi

if [[ ! -f "${DB_ARCHIVE}" ]]; then
  echo "Missing database archive: ${DB_ARCHIVE}" >&2
  exit 1
fi

echo "[1/6] Load bundled Docker images"
docker load -i "${IMAGE_ARCHIVE}"

echo "[2/6] Validate docker compose file"
export ANESTHESIA_CLOUDFLARED_TOKEN="${ANESTHESIA_CLOUDFLARED_TOKEN:-disabled-for-local-restore}"
docker compose -f "${COMPOSE_FILE}" config >/dev/null

echo "[3/6] Start infrastructure services"
docker compose -f "${COMPOSE_FILE}" up -d anesthesia-postgres anesthesia-redis anesthesia-mqtt

echo "[4/6] Restore PostgreSQL database"
until docker exec anesthesia-postgres pg_isready -U postgres -d anesthesia -p 5433 >/dev/null 2>&1; do
  sleep 2
done
gzip -dc "${DB_ARCHIVE}" | docker exec -i anesthesia-postgres psql -U postgres -p 5433 -d anesthesia

echo "[5/6] Start application services"
docker compose -f "${COMPOSE_FILE}" up -d --no-build anesthesia-base anesthesia-app anesthesia-gateway

echo "[6/6] Current service status"
docker compose -f "${COMPOSE_FILE}" ps

echo "Restore complete. Open http://127.0.0.1:8080"
