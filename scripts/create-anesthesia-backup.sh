#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="/home/kbzz1/20260131/backend"
BACKUP_ROOT="${ROOT_DIR}/backups"
TIMESTAMP="$(date +%Y%m%d_%H%M%S)"
BUNDLE_NAME="anesthesia_portable_backup_${TIMESTAMP}"
STAGE_DIR="${BACKUP_ROOT}/${BUNDLE_NAME}"
ARCHIVE_PATH="${BACKUP_ROOT}/${BUNDLE_NAME}.zip"
IMAGE_ARCHIVE="${STAGE_DIR}/docker-images/anesthesia-images.tar"
DB_ARCHIVE="${STAGE_DIR}/database/anesthesia.sql.gz"
METADATA_FILE="${STAGE_DIR}/backup-metadata.txt"

IMAGE_LIST=(
  anesthesia-app:latest
  anesthesia-base:latest
  nginx:1.27-alpine
  postgres:17-alpine
  redis:7.4-alpine
  eclipse-mosquitto:2.0
  cloudflare/cloudflared:latest
)

copy_tree() {
  local source_path="$1"
  local destination_path="$2"

  mkdir -p "${destination_path}"
  rsync -a \
    --exclude '.git' \
    --exclude 'target' \
    --exclude '.m2-local' \
    --exclude 'app.log' \
    --exclude '*.log' \
    --exclude '.DS_Store' \
    "${source_path}/" "${destination_path}/"
}

rm -rf "${STAGE_DIR}"
mkdir -p "${STAGE_DIR}/docker-images" "${STAGE_DIR}/database" "${STAGE_DIR}/scripts" "${BACKUP_ROOT}"

echo "[1/7] Copy project files"
copy_tree "${ROOT_DIR}/Backend-Anesthesia" "${STAGE_DIR}/code/Backend-Anesthesia"
copy_tree "${ROOT_DIR}/project" "${STAGE_DIR}/code/project"
copy_tree "${ROOT_DIR}/deploy" "${STAGE_DIR}/deploy"
copy_tree "${ROOT_DIR}/sql" "${STAGE_DIR}/sql"

cp "${ROOT_DIR}/docker-compose.anesthesia.yml" "${STAGE_DIR}/"
cp "${ROOT_DIR}/docker-compose.anesthesia.hot.yml" "${STAGE_DIR}/"
cp "${ROOT_DIR}/ANESTHESIA_UNIFIED_DEPLOY.md" "${STAGE_DIR}/"
cp "${ROOT_DIR}/scripts/deploy-anesthesia.sh" "${STAGE_DIR}/scripts/"
cp "${ROOT_DIR}/scripts/deploy-anesthesia-hot.sh" "${STAGE_DIR}/scripts/"
cp "${ROOT_DIR}/scripts/restore-anesthesia-backup.sh" "${STAGE_DIR}/scripts/"

echo "[2/7] Capture metadata"
{
  echo "backup_name=${BUNDLE_NAME}"
  echo "created_at=$(date --iso-8601=seconds)"
  echo "host=$(hostname)"
  echo "root_dir=${ROOT_DIR}"
  echo
  echo "[docker_ps]"
  docker ps --format '{{.Names}} {{.Image}} {{.Status}}'
  echo
  echo "[docker_images]"
  docker images --format '{{.Repository}}:{{.Tag}} {{.ID}} {{.Size}}' | rg '^(anesthesia-app|anesthesia-base|nginx|postgres|redis|eclipse-mosquitto|cloudflare/cloudflared):'
} > "${METADATA_FILE}"

echo "[3/7] Export Docker images"
docker save -o "${IMAGE_ARCHIVE}" "${IMAGE_LIST[@]}"

echo "[4/7] Dump PostgreSQL database"
docker exec anesthesia-postgres pg_dump -U postgres -p 5433 -d anesthesia --clean --if-exists --no-owner --no-privileges | gzip -9 > "${DB_ARCHIVE}"

echo "[5/7] Write restore guide"
cat > "${STAGE_DIR}/README_RESTORE.md" <<'EOF'
# Anesthesia Portable Backup

This bundle contains:
- Source code for both backend services
- Docker compose files
- Pre-exported Docker images
- PostgreSQL database dump
- Restore scripts for a fresh WSL machine

## Requirements
- WSL2
- Docker Engine with Compose plugin
- `unzip`

## Quick Restore
1. Unzip this package into a writable directory in WSL.
2. `cd` into the extracted folder.
3. Run:

```bash
./scripts/restore-anesthesia-backup.sh
```

4. After restore, open:

```text
http://127.0.0.1:8080
```

## Notes
- `anesthesia-cloudflared` is not started by the restore script by default.
- The restore script uses the bundled Docker images and does not require internet access.
- TLS certs included here are the same files currently mounted by the source environment.
EOF

echo "[6/7] Create zip archive"
(
  cd "${BACKUP_ROOT}"
  rm -f "${ARCHIVE_PATH}"
  if command -v zip >/dev/null 2>&1; then
    zip -qry "${ARCHIVE_PATH}" "${BUNDLE_NAME}"
  else
    jar --create --file "${ARCHIVE_PATH}" "${BUNDLE_NAME}"
  fi
)

echo "[7/7] Completed"
echo "Bundle directory: ${STAGE_DIR}"
echo "Zip archive: ${ARCHIVE_PATH}"
