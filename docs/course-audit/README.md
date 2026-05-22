# Course Alignment Audit

## Purpose

This folder documents the S26 audit that maps the current CampusEnroll HA repository to the topics present in the provided Database II course ZIP.

The goal is not to rewrite the platform. The goal is to show, with evidence from the repository, which course topics are already covered, which topics are only partially represented, which topics are only documented, and which topics should remain future production work.

## Audit Scope

This audit was produced from:

- the current repository under `campusenroll-ha/`
- the provided course ZIP, inspected as read-only reference material outside the repo
- the course export structure in `course-data.js`
- the bundled reference assets `coreografy.zip` and `PostgreSQL_Performance_Engineering.pdf`

This audit intentionally keeps the following boundaries:

- no Java runtime code changes
- no SQL schema rewrite
- no Docker Compose rewrite
- no k6 script rewrite
- no backup script rewrite
- no HAProxy rewrite
- no frontend claims, because frontend remains intentionally out of scope

## How To Read This Audit

- `COURSE_ALIGNMENT_MATRIX.md` is the main topic-by-topic mapping.
- `GAP_ANALYSIS.md` groups the current strengths and the most important missing pieces.
- `IMPLEMENTATION_RECOMMENDATIONS.md` prioritizes what is worth doing before presentation versus what should stay future work.
- `PRESENTATION_COVERAGE_MAP.md` translates the technical mapping into demo talking points.
- `CODEX_AUDIT_SUMMARY.md` records what was inspected and the final audit conclusion.

## S27 Evidence Hardening Package

These S27 files turn the S26 gap analysis into presentation-ready support material without changing runtime code or infrastructure:

- `EXPLAIN_AND_INDEX_EVIDENCE.md`
- `TRANSACTIONS_CONCURRENCY_IDEMPOTENCY.md`
- `DCL_SECURITY_NOTES.md`
- `SLO_ALERTING_PROPOSAL.md`
- `HA_TERMINOLOGY_AND_LIMITS.md`
- `S27_EVIDENCE_SUMMARY.md`

## High-Level Mapping To Database II

CampusEnroll HA already aligns strongly with the course in the following areas:

- Database as code through `db/schema.sql`, `db/data.sql`, and a centralized PostgreSQL model.
- Relational design through foreign keys, check constraints, and partial unique indexes.
- Transactional service logic through `@Transactional` service methods in the implemented services.
- Distributed application structure through Spring Boot microservices and Compose-based packaging.
- Redis as a complement to PostgreSQL through catalog caching in `course-service`.
- RabbitMQ messaging through publisher and consumer flows shared by `enrollment-service`, `billing-service`, and `notification`.
- Observability foundations through Actuator metrics and Prometheus scraping.
- Stress and concurrency testing through k6 load assets, including a dedicated concurrent enrollment scenario.
- Backup, restore, and continuity through PowerShell scripts and a PostgreSQL disaster recovery runbook.
- Application-level failover and switchover through HAProxy plus `course-service-replica`.

## What Is Already Implemented

The current repository contains working, repo-backed implementations for:

- a centralized PostgreSQL database model
- deterministic schema and seed SQL
- normalized relational constraints and indexes
- Redis-backed catalog caching with fallback behavior
- RabbitMQ event publication and consumption for evidence-oriented asynchronous flow
- Prometheus metrics exposure in all five runtime services
- Grafana as available infrastructure
- k6 smoke, exact-load, and concurrent enrollment scripts
- manual PostgreSQL backup and restore
- local application failover and switchover for `course-service`

## What Is Documented But Not Fully Implemented

Several course topics are present only as explanation, planning, or future recommendations, not as full runtime features:

- explicit CampusEnroll demonstrations of MVCC and isolation-level behavior
- explicit locking patterns such as `FOR UPDATE` or `SKIP LOCKED`
- full saga compensation logic
- idempotency keys and durable outbox style delivery
- DCL role/grant scripts
- EXPLAIN, BUFFERS, and CPU-vs-I/O evidence packages for current queries
- SLO definitions and active Prometheus alert rules
- PostgreSQL replication, read replicas, and database failover

## What Should Stay Future Production Work

The following topics belong in a future production roadmap, not in a last-minute academic hardening pass:

- PostgreSQL streaming replication and automatic failover
- multi-node deployment beyond the local demo shape
- broad partitioning or anti-hotspot redesign
- full cross-service saga compensation and outbox infrastructure
- production-grade dashboards, alerts, escalation paths, and on-call workflow
- HA coverage for every service behind a unified gateway

## Important Interpretation Notes

- Centralized PostgreSQL is not a weakness in this audit. It is aligned with the course focus and the repo's current academic scope.
- Application failover is not the same as database failover.
- Backup and restore are not the same as automatic failover.
- RabbitMQ event flow here is closer to lightweight choreography than to a complete saga platform.
- Grafana is available infrastructure, and Prometheus alert rules are now implemented, but Alertmanager routing and production-complete dashboards are still incomplete.
- Older S00 and early-planning files still exist in the repo. They are useful as history, but they are not authoritative evidence of the current runtime state.

## Legacy Planning Files To Treat Carefully

The following files are historical planning material and should not be used alone to claim current implementation coverage:

- `docs/CODEX_CONTEXT.md`
- `docs/SERVICE_BOUNDARIES.md`
- `backend/gateway-service/README.md`
- `infra/prometheus/README.md`
- `infra/grafana/README.md`
- older diagram files under `docs/diagrams/`

Use the matrix in this folder together with the current runtime docs, backend code, `db/`, `infra/`, and `README.md` when presenting the real state of CampusEnroll HA.
