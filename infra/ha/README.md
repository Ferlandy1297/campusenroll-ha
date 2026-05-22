# Application Failover Demo Scripts

This folder contains the PowerShell helpers for the S25 application-level failover and switchover demo in CampusEnroll HA.

## Scope

- Uses `docker-compose.ha-demo.yml` on top of the existing infrastructure and app layers.
- Exercises HAProxy on `http://localhost:8080`.
- Demonstrates continuity for the `course-service` catalog routes when the primary instance is stopped.
- Leaves PostgreSQL failover out of scope. Database recovery still relies on the backup and restore scripts in `infra/backups/`.

## Files

- `failover-demo.ps1`: stops the primary `course-service`, checks that the HAProxy gateway still returns HTTP 200, then starts the primary again
- `switchover-demo.ps1`: performs the same traffic handoff as a planned maintenance switchover and checks both `/api/courses` and `/health/course`

## Prerequisite

Start the HA demo stack first:

```powershell
docker compose -f docker-compose.yml -f docker-compose.apps.yml -f docker-compose.ha-demo.yml up -d --build
docker compose -f docker-compose.yml -f docker-compose.apps.yml -f docker-compose.ha-demo.yml ps
```

## Quick Commands

Open the HAProxy stats page:

```powershell
Start-Process "http://localhost:8404/stats"
```

Run the failover demo:

```powershell
powershell -ExecutionPolicy Bypass -File infra/ha/failover-demo.ps1 -OpenStats
```

Run the planned switchover demo:

```powershell
powershell -ExecutionPolicy Bypass -File infra/ha/switchover-demo.ps1 -OpenStats
```

## Notes

- A successful `HTTP 200` from `http://localhost:8080/api/courses` after `course-service` is stopped means HAProxy is serving `course-service-replica`.
- The HAProxy stats page is intentionally open and local-only for academic demo use.
- This is application-service continuity. It does not implement PostgreSQL replication, Patroni, repmgr, pg_auto_failover, or a managed database failover service.
