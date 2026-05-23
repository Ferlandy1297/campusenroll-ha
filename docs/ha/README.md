# Application Failover Docs

`docs/ha/` groups the S25 material for application-level failover and switchover in CampusEnroll HA.

## Files

- `APPLICATION_FAILOVER_SWITCHOVER_DEMO.md`
  - main explanation of the HAProxy layer, startup command, demo flow, and current scope
- `POSTGRES_REPLICATION_FAILOVER_DEMO.md`
  - isolated PostgreSQL primary or replica demo, streaming replication checks, manual failover, and manual switchover wording
- `PRESENTATION_QA.md`
  - short answers for common presentation or viva questions

## Core Message

- CampusEnroll HA now demonstrates application-level failover and planned switchover for `course-service`.
- The demo uses HAProxy plus `course-service-replica`.
- PostgreSQL still remains centralized for the main application stack.
- S32 now adds a separate PostgreSQL streaming replication demo with manual replica promotion.
- The main stack keeps its `.env` PostgreSQL port, currently `55432`, while the isolated S32 demo uses `56432` and `56433`.
- Automatic PostgreSQL failover is still not implemented; backup and restore remain the main data recovery layer for the application stack.
