# Application Failover Docs

`docs/ha/` groups the S25 material for application-level failover and switchover in CampusEnroll HA.

## Files

- `APPLICATION_FAILOVER_SWITCHOVER_DEMO.md`
  - main explanation of the HAProxy layer, startup command, demo flow, and current scope
- `PRESENTATION_QA.md`
  - short answers for common presentation or viva questions

## Core Message

- CampusEnroll HA now demonstrates application-level failover and planned switchover for `course-service`.
- The demo uses HAProxy plus `course-service-replica`.
- PostgreSQL remains centralized by academic requirement.
- PostgreSQL failover is not implemented; recovery still depends on backup and restore.
