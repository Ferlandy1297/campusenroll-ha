# S27 Evidence Summary

## What S27 adds

S27 adds a documentation-only evidence hardening package on top of the S26 course alignment audit.

New documentation areas:

- `EXPLAIN_AND_INDEX_EVIDENCE.md`
- `TRANSACTIONS_CONCURRENCY_IDEMPOTENCY.md`
- `DCL_SECURITY_NOTES.md`
- `SLO_ALERTING_PROPOSAL.md`
- `HA_TERMINOLOGY_AND_LIMITS.md`

## How S27 closes the S26 gaps

### EXPLAIN, ANALYZE, and BUFFERS evidence

Closed by:

- `docs/course-audit/EXPLAIN_AND_INDEX_EVIDENCE.md`

What it adds:

- PowerShell-ready `docker exec` and `psql` commands
- representative CampusEnroll queries
- plan interpretation guidance
- index rationale tied to the real schema

### Index rationale

Closed by:

- `docs/course-audit/EXPLAIN_AND_INDEX_EVIDENCE.md`

What it adds:

- explanation of active-state indexes
- join-path indexes
- time-oriented indexes
- partial unique business-rule indexes

### MVCC, ACID, isolation, and concurrency explanation

Closed by:

- `docs/course-audit/TRANSACTIONS_CONCURRENCY_IDEMPOTENCY.md`

What it adds:

- clear ACID interpretation for this project
- MVCC explanation tied to PostgreSQL
- Spring `@Transactional` mapping
- honest limits around isolation tuning and locking

### Anti-oversell and idempotency explanation

Closed by:

- `docs/course-audit/TRANSACTIONS_CONCURRENCY_IDEMPOTENCY.md`

What it adds:

- precise explanation of duplicate-active enrollment protection
- precise explanation of duplicate-pending billing protection
- clear statement that this is not yet full seat-inventory anti-oversell
- clear statement that this is not yet full idempotency keys or outbox

### DCL and database security explanation

Closed by:

- `docs/course-audit/DCL_SECURITY_NOTES.md`

What it adds:

- DCL definitions
- least-privilege explanation
- honest explanation of the current local credential model
- illustrative role and grant examples for discussion

### SLO and alerting proposal

Closed by:

- `docs/course-audit/SLO_ALERTING_PROPOSAL.md`

What it adds:

- SLI, SLO, SLA, and alerting terminology
- proposed academic objectives
- example Prometheus alert rules
- honest boundary between metrics and active alerting

### HA terminology and precise limits

Closed by:

- `docs/course-audit/HA_TERMINOLOGY_AND_LIMITS.md`

What it adds:

- clean definitions
- presentation-safe phrasing
- trap-question answers
- explicit distinction between application failover, database failover, backup, restore, RPO, and RTO

## What remains future production work

S27 does not change the technical boundary established in S26.

Still future work:

- PostgreSQL streaming replication
- automatic PostgreSQL failover
- database switchover
- read replicas
- full seat-capacity reservation logic
- explicit `SKIP LOCKED` or `FOR UPDATE` based workflows
- deadlock retry framework
- idempotency keys
- outbox pattern
- full saga compensation
- active Prometheus alert wiring
- finished Grafana dashboards
- multi-node production cluster

## What the team should show in the final presentation

Best high-value evidence set:

- `db/schema.sql` and `db/data.sql`
- selected index definitions from `db/schema.sql`
- `@Transactional` examples in enrollment and billing services
- Redis cache evidence for `GET /api/courses`
- RabbitMQ exchange, queue, and event logs
- k6 concurrent enrollment script and result
- Prometheus targets and one `/actuator/prometheus` endpoint
- backup and restore scripts plus the disaster recovery runbook
- HAProxy failover or switchover demo
- selected S27 docs when answering theory-heavy questions

## Concise checklist

- [ ] Use `EXPLAIN_AND_INDEX_EVIDENCE.md` if query-plan questions come up.
- [ ] Use `TRANSACTIONS_CONCURRENCY_IDEMPOTENCY.md` for MVCC, ACID, anti-oversell, and idempotency questions.
- [ ] Use `DCL_SECURITY_NOTES.md` for least-privilege and DCL questions.
- [ ] Use `SLO_ALERTING_PROPOSAL.md` for observability maturity and alerting questions.
- [ ] Use `HA_TERMINOLOGY_AND_LIMITS.md` for failover, switchover, backup, restore, RPO, and RTO questions.
- [ ] Keep the final narrative honest: implemented now versus future production work.

## Bottom line

S27 does not try to make CampusEnroll HA look larger than it is.

It makes the current implementation easier to defend by giving the team:

- stronger Database II language
- clearer evidence commands
- better index and concurrency explanations
- safer HA terminology
- cleaner answers during the final presentation
