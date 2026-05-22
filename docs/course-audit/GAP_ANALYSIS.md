# Gap Analysis

## A. Already covered strongly

- Docker and layered local deployment are strong. The repo has `docker-compose.yml`, `docker-compose.apps.yml`, and `docker-compose.ha-demo.yml`, which makes the infrastructure, app stack, and HA demo shape easy to explain.
- Microservice packaging is strong. `student-service`, `course-service`, `enrollment-service`, `billing-service`, and `notification` are all implemented as Spring Boot services with service-level docs and health endpoints.
- PostgreSQL schema and seed coverage are strong. `db/schema.sql` and `db/data.sql` give the course a concrete relational artifact with keys, checks, indexes, and deterministic demo data.
- Redis coverage is strong enough for the course. `course-service` uses Redis for catalog caching and includes a documented fallback path when Redis is unavailable.
- RabbitMQ coverage is strong enough for messaging fundamentals. Enrollment and billing publish events, and notification consumes them through a shared exchange and queue bindings.
- Prometheus metrics coverage is strong. All five services expose `/actuator/prometheus`, and Prometheus is configured to scrape them in the HA-readiness mode.
- k6 coverage is strong. The repo includes smoke, exact-load, and concurrent enrollment scripts plus a manual failure observation guide.
- Backup and restore coverage is strong. The PowerShell scripts plus the disaster recovery runbook give the project a concrete continuity story for PostgreSQL.
- HAProxy application failover coverage is strong. `course-service` has a local primary and replica plus scripted failover and switchover demos.

## B. Covered but needs better explanation or documentation

- MVCC and ACID are present through PostgreSQL and transactional service methods, but the repo does not yet explain them in CampusEnroll terms. The team should connect the theory to `@Transactional` code and the centralized database.
- Isolation-level coverage is implicit, not explicit. PostgreSQL behavior exists underneath the system, but the repo does not show a short CampusEnroll-specific explanation of `READ COMMITTED`, repeatable reads, or serialization tradeoffs.
- Idempotency wording now needs a narrower explanation. S29 implements `Idempotency-Key` for selected critical endpoints, but the repo still does not implement a transactional outbox or full endpoint coverage.
- Saga versus choreography needs a careful explanation. RabbitMQ is real in the repo, but the current flow is lightweight choreography for evidence, not a full compensated saga.
- RPO and RTO are documented in the disaster recovery runbook, but the team still needs to explain them clearly as local academic targets, not automated production guarantees.
- Academic HA versus production HA needs constant discipline. The project demonstrates application-level continuity for `course-service`, not database replication, cluster-wide failover, or platform-grade HA.
- Legacy document drift is real. Older S00 planning files and some diagrams still describe Redis, RabbitMQ, Prometheus, Grafana, and the gateway as planned rather than current or partially current.

## C. Possible low-risk improvements before final delivery

- Add a docs-only EXPLAIN appendix for two or three representative CampusEnroll queries. This would directly cover the course topic without changing runtime code.
- Add a docs-only index rationale note. The schema already has useful active, foreign-key, timestamp, and partial unique indexes, but the presentation would benefit from one page that explains why they exist.
- Add a docs-only concurrency note that ties `uq_enrollments_active_student_section`, `saveAndFlush`, and `infra/k6/concurrent-enrollment-test.js` together.
- Add a short demo note or screenshot set that shows the live `Idempotency-Key` replay and the `idempotency_records` table side by side.
- Add a docs-only DCL/security note. Even a small appendix with recommended roles and `GRANT` examples would improve alignment with the course without forcing schema change in this phase.
- S27 and S28 already closed the SLO and alerting documentation gap with a real Prometheus rules file plus updated presentation-safe wording.
- Refresh or annotate stale diagrams and early planning docs in a later docs-only segment so they stop understating the current runtime state.

## D. Production-level upgrades not recommended to implement now

- Do not add PostgreSQL streaming replication or automatic database failover now. That is a different risk profile than the rest of the repo and would require new operational guarantees.
- Do not redesign the system around a full saga plus outbox, retry, and DLQ platform now. That would widen the backend scope far beyond an audit-alignment pass.
- Do not introduce broad partitioning or anti-hotspot redesign now. The current data size and academic workload do not justify the migration risk.
- Do not attempt a Kubernetes migration, Docker Swarm migration, or true multi-node deployment now. The current Compose-based story is already presentation-worthy and much safer.
- Do not claim production-grade alerting, dashboards, or on-call workflow by adding thin placeholders. Those topics should either be evidence-backed or explicitly left future.

## Practical Conclusion

CampusEnroll HA is already strong in the topics that matter most for a Database II final review: centralized relational design, concurrency-aware service logic, Redis and RabbitMQ complements, observability foundations, stress testing, backup and restore, and application-level failover.

The main missing layer is not a big code rewrite. It is explanatory precision: a tighter bridge between course vocabulary and what the repo actually implements today.
