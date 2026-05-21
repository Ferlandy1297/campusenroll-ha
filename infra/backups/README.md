# PostgreSQL Backup Utilities

This folder contains the local academic backup and restore layer added in S22 for CampusEnroll HA.

## Scope

- Protects the PostgreSQL `campusenroll` database running in the `campusenroll-postgres` container.
- Uses PowerShell scripts designed for Windows execution from the repo root or directly from this folder.
- Stores generated dumps in `infra/backups/output/`.

## Files

- `backup-postgres.ps1`: creates a timestamped PostgreSQL custom-format dump with `pg_dump -Fc`
- `restore-postgres.ps1`: restores a selected dump with `pg_restore --clean --if-exists`
- `verify-database.ps1`: checks connectivity, current database/user, and key table counts
- `DISASTER_RECOVERY_RUNBOOK.md`: practical recovery guide for demos, checkpoints, and local rebuilds
- `output/.gitignore`: prevents generated backup artifacts from being committed

## Quick Commands

Start the stack:

```powershell
docker compose -f docker-compose.yml -f docker-compose.apps.yml up -d --build
```

Verify the database:

```powershell
powershell -ExecutionPolicy Bypass -File infra/backups/verify-database.ps1
```

Create a backup:

```powershell
powershell -ExecutionPolicy Bypass -File infra/backups/backup-postgres.ps1
```

List available backup files:

```powershell
Get-ChildItem infra/backups/output
```

Restore a backup:

```powershell
powershell -ExecutionPolicy Bypass -File infra/backups/restore-postgres.ps1 -BackupFile "infra/backups/output/<backup-file>.dump" -Force
```

## Notes

- Backups are local and academic. Production would require scheduling, off-site storage, encryption, access control, and tested retention policies.
- Frontend remains intentionally out of scope for this project. Postman is still the current client for manual validation.
