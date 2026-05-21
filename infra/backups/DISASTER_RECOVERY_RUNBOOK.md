# Disaster Recovery Runbook

## 1. Purpose

This runbook documents the local S22 backup, restore, and disaster recovery procedure for CampusEnroll HA.

Goal:

- protect the PostgreSQL `campusenroll` database used by the backend and microservices
- provide a repeatable backup command
- provide a guarded restore command
- leave a simple verification flow for checkpoints, demos, and local rebuilds

Frontend remains intentionally out of scope. Postman is still the operational client for this academic delivery.

## 2. What Is Protected

The backup layer protects the PostgreSQL data stored in the `campusenroll` database inside the `campusenroll-postgres` container, including the main relational tables used by the project:

- `students`
- `courses`
- `academic_periods`
- `sections`
- `schedule_blocks`
- `enrollments`
- `billings`

The dump is created in PostgreSQL custom format with `pg_dump -Fc`, which is appropriate for later restore with `pg_restore`.

## 3. What Is Not Protected

This S22 layer does not protect:

- Redis cache contents
- RabbitMQ queue state
- Prometheus or Grafana historical data
- local Maven caches, IDE settings, or OS-level Docker configuration
- files outside PostgreSQL
- any frontend, because this project intentionally does not include one

This is a local academic recovery layer, not a production disaster recovery platform.

## 4. Storage Location

Generated backup files are stored in:

```text
infra/backups/output/
```

That folder is intentionally protected with `output/.gitignore` so generated artifacts are not committed.

## 5. Why Backups Must Not Be Committed to Git

Backup files should not be committed because they:

- create large binary diffs
- make repository history noisy
- can expose local data snapshots
- are operational artifacts, not source code

Keep the repository for code, docs, compose files, and runbooks. Keep dumps as generated local evidence.

## 6. Exact Commands

Start the HA readiness stack:

```powershell
docker compose -f docker-compose.yml -f docker-compose.apps.yml up -d --build
```

Verify the current database:

```powershell
powershell -ExecutionPolicy Bypass -File infra/backups/verify-database.ps1
```

Create a backup:

```powershell
powershell -ExecutionPolicy Bypass -File infra/backups/backup-postgres.ps1
```

List backup files:

```powershell
Get-ChildItem infra/backups/output
```

Restore a backup:

```powershell
powershell -ExecutionPolicy Bypass -File infra/backups/restore-postgres.ps1 -BackupFile "infra/backups/output/<backup-file>.dump" -Force
```

Verify again after restore:

```powershell
powershell -ExecutionPolicy Bypass -File infra/backups/verify-database.ps1
```

## 7. Recovery Scenarios

### Scenario A: Accidental Data Loss

Example:

- rows were deleted by mistake
- a manual test changed critical records

Response:

1. Stop application writes if possible.
2. Run `verify-database.ps1` to inspect the current state.
3. Select the most recent valid backup from `infra/backups/output/`.
4. Run `restore-postgres.ps1 -Force`.
5. Run `verify-database.ps1` again and capture the result.

### Scenario B: Database Volume Corruption

Example:

- PostgreSQL volume is damaged
- container starts but data is unusable

Response:

1. Keep the latest valid dump file safe outside the damaged volume.
2. Stop the stack.
3. Recreate the PostgreSQL container and volume through Docker Compose.
4. Start the database again.
5. Run the restore command with a valid dump.
6. Run the verification script and confirm key tables are visible again.

### Scenario C: Local Environment Rebuild

Example:

- Docker Desktop was reset
- the machine changed
- the project was re-cloned and the local environment must be rebuilt

Response:

1. Start the stack with the Compose command.
2. Confirm `campusenroll-postgres` is running.
3. Restore the selected dump file.
4. Run the verification script.
5. Continue with Postman, Prometheus, Grafana, or k6 validation as needed.

## 8. RPO and RTO in Simple Academic Terms

- `RPO` (Recovery Point Objective): how much data you can afford to lose between the last backup and the incident. In this local academic setup, the practical RPO is the time between manual backups.
- `RTO` (Recovery Time Objective): how long it takes to bring the database back to a usable state. In this setup, the practical RTO is the time to start PostgreSQL, copy the dump, run `pg_restore`, and verify the tables.

Suggested classroom wording:

`CampusEnroll HA now has a documented local RPO/RTO story for PostgreSQL, but it still depends on manual execution and local storage rather than automated production controls.`

## 9. Recommended Backup Frequency

For this project, the minimum practical recommendation is:

- create a backup before each checkpoint or final demo
- create a backup after loading a clean dataset that you want to preserve
- create a backup before any risky manual testing or restore exercise

## 10. Recommended Retention

For a local academic workflow, keep at least:

- the latest 3 to 5 successful dumps in `infra/backups/output/`
- one known-good dump copied outside the repo if the team needs extra safety

Production retention would need a formal policy, automation, and monitored storage.

## 11. Restore Safety Notes

- `restore-postgres.ps1` requires `-Force` to reduce accidental execution.
- The restore uses `pg_restore --clean --if-exists`, so current database objects are dropped and recreated from the selected dump.
- It is safer to stop local microservices or app containers before restoring to avoid concurrent writes during the operation.

## 12. Evidence to Capture for the Final Presentation

Capture these items:

1. `verify-database.ps1` before backup
2. `backup-postgres.ps1` success message with generated filename
3. `Get-ChildItem infra/backups/output`
4. the runbook file open in the editor
5. the restore command with `-Force` visible
6. `verify-database.ps1` after restore

Recommended screenshot names:

- `backup_verify_before.png`
- `backup_created.png`
- `backup_output_list.png`
- `runbook_open.png`
- `restore_command.png`
- `backup_verify_after.png`

## 13. Honest Scope Statement

S22 improves operational continuity and high availability readiness by adding a practical PostgreSQL backup and restore layer. It does not add:

- automated scheduled backups
- off-site replication
- encrypted backup storage
- production retention enforcement
- automatic failover

Those remain future improvements beyond this local academic implementation.
