# PostgreSQL HA Demo - S32

This folder contains the isolated PostgreSQL streaming replication demo added in S32 for CampusEnroll HA.

## Scope

- Adds a standalone PostgreSQL primary plus read replica demo.
- Uses `docker-compose.db-ha-demo.yml`.
- Does not replace `campusenroll-postgres` from `docker-compose.yml`.
- Does not repoint any backend service to the demo topology.
- Demonstrates manual failover by promoting the replica.
- Documents manual switchover as an operational procedure.

## Topology

- primary container: `campusenroll-pg-primary`
- replica container: `campusenroll-pg-replica`
- primary host port: `56432`
- replica host port: `56433`
- primary volume: `campusenroll_pg_primary_data`
- replica volume: `campusenroll_pg_replica_data`
- demo database: `campusenroll_ha_demo`
- demo table: `replication_probe`

## Important Boundary

The main application database in `docker-compose.yml` uses the `.env` host port, currently `55432`.

This means:

- the S32 demo is isolated and started separately
- backend services are not switched to the demo primary or replica
- the S32 demo uses `56432` and `56433`, so it does not share the main stack host port
- the canonical S33 validation path uses the explicit `docker compose -f docker-compose.db-ha-demo.yml ...` commands documented under `docs/ha/POSTGRES_REPLICATION_FAILOVER_DEMO.md`

## Files

- `primary/init/01-configure-primary.sh`
  - configures replication access on the primary
- `primary/init/02-replication-probe.sql`
  - creates the visible demo table
- `replica/entrypoint.sh`
  - waits for the primary, runs `pg_basebackup -R`, and starts hot standby
- `start-postgres-ha-demo.ps1`
  - starts the isolated demo and optionally stops the main PostgreSQL container if you pass `-ForceStopMainPostgres`
- `verify-replication.ps1`
  - checks primary or replica roles, streaming state, and optionally inserts a probe row
- `failover-promote-replica.ps1`
  - stops the primary, promotes the replica, and verifies write acceptance
- `reset-postgres-ha-demo.ps1`
  - tears down the demo and removes both named volumes

## Quick Commands

Validate the Compose file:

```powershell
docker compose -f docker-compose.db-ha-demo.yml config
```

Start the demo safely:

```powershell
powershell -ExecutionPolicy Bypass -File infra/postgres-ha/start-postgres-ha-demo.ps1
```

Verify streaming replication:

```powershell
powershell -ExecutionPolicy Bypass -File infra/postgres-ha/verify-replication.ps1 -InsertProbe
```

Promote the replica manually:

```powershell
powershell -ExecutionPolicy Bypass -File infra/postgres-ha/failover-promote-replica.ps1
```

Reset the demo:

```powershell
powershell -ExecutionPolicy Bypass -File infra/postgres-ha/reset-postgres-ha-demo.ps1
```

## Limits

- This is a local manual demo, not automatic failover.
- This is not Patroni, repmgr, pg_auto_failover, Kubernetes, or a production multi-node database cluster.
- Production-grade database HA would still require leader election, fencing, client rerouting, monitoring, and tested operational procedures.
