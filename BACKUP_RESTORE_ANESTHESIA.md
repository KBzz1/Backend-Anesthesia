# Backup And Restore

Create a portable offline bundle:

```bash
cd /home/kbzz1/20260131/backend
./scripts/create-anesthesia-backup.sh
```

The script produces:
- a staging directory under `backups/`
- a single zip archive under `backups/`

Bundle contents:
- source code
- compose files
- deploy scripts
- Docker image export
- PostgreSQL dump
- restore script

Restore on another WSL machine:

```bash
unzip anesthesia_portable_backup_*.zip
cd anesthesia_portable_backup_*/
./scripts/restore-anesthesia-backup.sh
```

Default restore behavior:
- does not start `anesthesia-cloudflared`
- restores PostgreSQL from the bundled dump
- starts `anesthesia-postgres`, `anesthesia-redis`, `anesthesia-mqtt`, `anesthesia-base`, `anesthesia-app`, and `anesthesia-gateway`
- serves locally on `http://127.0.0.1:8080`
