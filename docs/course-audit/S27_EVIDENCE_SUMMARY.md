# S27 Evidence Summary

## What S27 adds

S27 added a documentation-only evidence hardening package on top of the S26 course alignment audit.

New documentation areas:

- `EXPLAIN_AND_INDEX_EVIDENCE.md`
- `TRANSACTIONS_CONCURRENCY_IDEMPOTENCY.md`
- `DCL_SECURITY_NOTES.md`
- `SLO_ALERTING_PROPOSAL.md`
- `HA_TERMINOLOGY_AND_LIMITS.md`

## What S28 changes on top of S27

S28 turns the observability section from proposal-only wording into a real Prometheus implementation.

It adds:

- `infra/prometheus/rules/campusenroll-alerts.yml`
- `rule_files` loading in `infra/prometheus/prometheus.yml`
- mounted rules in `docker-compose.yml`
- live Prometheus rules for service-down, target-missing, HTTP 5xx rate, and p95 latency

## What S29 changes on top of S28

S29 turns idempotency from partial relational protection into a real API feature for selected critical writes.

It adds:

- PostgreSQL `idempotency_records` in `db/schema.sql`
- `Idempotency-Key` support for `POST /api/enrollments`
- `Idempotency-Key` support for `POST /api/billings`
- stored response replay for successful same-key retries
- `409 Conflict` protection for same-key different-payload reuse
- tests for replay and payload mismatch in enrollment and billing services

## How S27 closed the S26 gaps

### EXPLAIN, ANALYZE, and BUFFERS evidence

Closed by:

- `docs/course-audit/EXPLAIN_AND_INDEX_EVIDENCE.md`

### Index rationale

Closed by:

- `docs/course-audit/EXPLAIN_AND_INDEX_EVIDENCE.md`

### MVCC, ACID, isolation, and concurrency explanation

Closed by:

- `docs/course-audit/TRANSACTIONS_CONCURRENCY_IDEMPOTENCY.md`

### Anti-oversell and idempotency explanation

Closed by:

- `docs/course-audit/TRANSACTIONS_CONCURRENCY_IDEMPOTENCY.md`

### DCL and database security explanation

Closed by:

- `docs/course-audit/DCL_SECURITY_NOTES.md`

### SLO and alerting

Closed first by:

- `docs/course-audit/SLO_ALERTING_PROPOSAL.md`

Then hardened in runtime by S28 through:

- `infra/prometheus/rules/campusenroll-alerts.yml`
- `infra/prometheus/prometheus.yml`

### HA terminology and precise limits

Closed by:

- `docs/course-audit/HA_TERMINOLOGY_AND_LIMITS.md`

## What remains future production work

Still future work:

- PostgreSQL streaming replication
- automatic PostgreSQL failover
- database switchover
- read replicas
- full seat-capacity reservation logic
- explicit `SKIP LOCKED` or `FOR UPDATE` workflows
- deadlock retry framework
- outbox pattern
- full saga compensation
- idempotency coverage for every write endpoint
- Alertmanager routing and external notifications
- backup freshness metric export for alerting
- finished Grafana dashboards
- multi-node production cluster

## What the team should show in the final presentation

Best high-value evidence set:

- `db/schema.sql` and `db/data.sql`
- selected index definitions from `db/schema.sql`
- `@Transactional` examples in enrollment and billing services
- `Idempotency-Key` retry and payload-mismatch examples for enrollments and billings
- Redis cache evidence for `GET /api/courses`
- RabbitMQ exchange, queue, and event logs
- k6 concurrent enrollment script and result
- Prometheus targets plus the `/alerts` and `/rules` pages
- backup and restore scripts plus the disaster recovery runbook
- HAProxy failover or switchover demo
- selected S27 support docs for theory-heavy questions

## Concise checklist

- [ ] Use `EXPLAIN_AND_INDEX_EVIDENCE.md` if query-plan questions come up.
- [ ] Use `TRANSACTIONS_CONCURRENCY_IDEMPOTENCY.md` for MVCC, ACID, anti-oversell, and idempotency questions.
- [ ] Explain the exact S29 boundary: selected API idempotency is implemented, transactional outbox belongs to S30, and full saga compensation belongs to S31.
- [ ] Use `DCL_SECURITY_NOTES.md` for least-privilege and DCL questions.
- [ ] Use `SLO_ALERTING_PROPOSAL.md` for observability maturity, active Prometheus rules, and current alerting limits.
- [ ] Use `HA_TERMINOLOGY_AND_LIMITS.md` for failover, switchover, backup, restore, RPO, and RTO questions.
- [ ] Keep the final narrative honest: implemented now versus future production work.

## Bottom line

S27 made the project easier to defend.

S28 makes one of those defenses operational:

- real metrics
- real scrape targets
- real Prometheus alert rules
- no Alertmanager claim
- no production dashboard claim

S29 makes another defense operational:

- real `Idempotency-Key` support on selected critical write endpoints
- real PostgreSQL-backed response replay
- no outbox claim in S29
- no full saga compensation claim in S29
