# HA Terminology And Limits

## Purpose

This document gives the team a precise, presentation-safe vocabulary for CampusEnroll HA.

Its main objective is to prevent the most common final-review mistake:

- overclaiming application continuity as if it were database failover or production-grade high availability

## Core definitions

| Term | Meaning |
| --- | --- |
| `healthcheck` | A technical probe used to determine whether a service is responding in a way considered healthy enough for runtime use. |
| `restart policy` | A container runtime rule that tells Docker when to restart a container after failure or stop conditions. |
| `backup` | A saved copy of data that can be used later for recovery. |
| `restore` | The act of rebuilding database state from a backup artifact. |
| `RPO` | Recovery Point Objective: how much recent data loss is acceptable after an incident. |
| `RTO` | Recovery Time Objective: how long recovery can take before the system becomes usable again. |
| `failover` | Unplanned traffic or role handoff when the primary component becomes unavailable. |
| `switchover` | Planned traffic or role handoff for maintenance or controlled testing. |
| `replication` | Keeping data synchronized from a primary database to one or more secondary databases. |
| `read replica` | A replica database primarily used to serve reads rather than accept primary writes. |
| `load balancer` | A component that distributes incoming traffic across one or more backend services. |

## What CampusEnroll HA implements today

### 1. Healthchecks

Implemented now:

- PostgreSQL, Redis, RabbitMQ, Prometheus, and Grafana healthchecks in `docker-compose.yml`
- service healthchecks in `docker-compose.apps.yml`
- HAProxy backend health checks for `course-service` and `course-service-replica`

### 2. Restart policy

Implemented now:

- `restart: unless-stopped` for infrastructure and app containers in the Compose layers

### 3. Backup and restore for PostgreSQL

Implemented now:

- `infra/backups/backup-postgres.ps1`
- `infra/backups/restore-postgres.ps1`
- `infra/backups/verify-database.ps1`
- `infra/backups/DISASTER_RECOVERY_RUNBOOK.md`

This is a real continuity control, but it is data recovery, not automatic high availability.

### 4. Application-level failover

Implemented now:

- `docker-compose.ha-demo.yml`
- `infra/load-balancer/haproxy.cfg`
- `infra/ha/failover-demo.ps1`

The implemented failover is:

- application-level
- limited to the `course-service` catalog routes shown in the HA demo
- driven by HAProxy health checks

### 5. Application-level switchover

Implemented now:

- `infra/ha/switchover-demo.ps1`
- the planned maintenance traffic-handoff demo for `course-service`

This is a controlled handoff at the application layer, not a database role change.

## What CampusEnroll HA does not implement

The project does not currently implement:

- automatic PostgreSQL failover
- PostgreSQL streaming replication
- database switchover
- read replicas
- a multi-node production cluster
- Kubernetes
- platform-wide HA for every service behind one load balancer

These topics belong to future production work, not to the current verified scope.

## The most important distinctions

### Application failover is not database failover

Current truth:

- HAProxy can move catalog traffic from `course-service` to `course-service-replica`
- both application instances still use the same centralized PostgreSQL database

So if PostgreSQL fails, the current HAProxy demo does not solve that incident.

### Backup and restore is not failover

Current truth:

- backup and restore can recover data
- they do not keep the database continuously available during a failure
- they involve recovery time and possible recovery-point loss depending on when the last backup was taken

### Switchover is not restore

Current truth:

- switchover is a planned traffic handoff
- restore is data recovery from a backup artifact

### Restart policy is not high availability by itself

Current truth:

- `restart: unless-stopped` improves local resilience
- it does not create replicas
- it does not create database failover
- it does not replace an HA architecture

## Presentation-safe wording

Use wording like this:

`CampusEnroll HA currently demonstrates application-level failover and switchover for course-service through HAProxy and a replica instance. PostgreSQL remains centralized. Database continuity is currently handled through backup and restore, not through replication or automatic database failover.`

Backup and recovery wording:

`The project already has a documented local RPO and RTO story through manual PostgreSQL backup and restore, but that is different from automatic failover and should not be described as production-grade HA.`

Observability wording:

`Prometheus metrics and scrape targets are implemented. Grafana is available as infrastructure. Dashboards and active alerting are still future hardening work.`

## Trap-question answers

### Is this PostgreSQL failover?

No.

Correct answer:

`No. The repo demonstrates application-level failover for course-service. PostgreSQL failover is not implemented.`

### Is `course-service-replica` a database replica?

No.

Correct answer:

`No. It is an application replica of course-service. Both application instances still use the same centralized PostgreSQL database.`

### Does backup mean the system is highly available?

No.

Correct answer:

`No. Backup and restore support recovery after failure. They do not keep the database continuously available during failure.`

### Is this Kubernetes?

No.

Correct answer:

`No. The current implementation uses Docker Compose and HAProxy, not Kubernetes.`

### Does HAProxy balance all microservices in the project?

No.

Correct answer:

`No. The current HAProxy demo is intentionally narrow and only fronts the course-service catalog routes used in the S25 application failover demo.`

### Does the project have read replicas?

No.

Correct answer:

`No. Read replicas are discussed as future work, but they are not implemented in the current repository.`

### Does `restart: unless-stopped` mean the system is fully HA?

No.

Correct answer:

`No. Restart policy improves local recoverability, but full HA requires additional architecture such as replicas, coordinated failover, and database redundancy.`

## Useful reference files

- `README.md`
- `docs/ha/APPLICATION_FAILOVER_SWITCHOVER_DEMO.md`
- `docs/ha/PRESENTATION_QA.md`
- `infra/ha/README.md`
- `infra/backups/DISASTER_RECOVERY_RUNBOOK.md`

## Bottom line

CampusEnroll HA already has a defendable high-availability readiness story for the course, but only if the terminology stays precise.

The safest summary is:

- real healthchecks
- real restart policies
- real backup and restore
- real application-level failover for `course-service`
- no database failover
- no replication
- no production cluster claim
