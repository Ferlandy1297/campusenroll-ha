# Implementation Recommendations

## Must-have before presentation

| Item | Reason | Risk level | Files likely affected | Verification command | Presentation value |
| --- | --- | --- | --- | --- | --- |
| Add a docs-only EXPLAIN and index evidence appendix | This is the cleanest way to cover EXPLAIN, BUFFERS, CPU vs I/O, and index topics without touching runtime code. | Low | `docs/course-audit/*`; optionally `docs/demo/*` | `docker exec -i campusenroll-postgres psql -U campus -d campusenroll -c "EXPLAIN (ANALYZE, BUFFERS) SELECT * FROM courses WHERE active = true ORDER BY course_code;"` | High: directly closes one of the biggest course-topic gaps. |
| Add a docs-only concurrency note for duplicate-active enrollment protection | The repo already has a partial unique index, service-level guard, and k6 concurrency script; the missing piece is a single coherent explanation. | Low | `docs/course-audit/*`; optionally `docs/demo/DEMO_SCRIPT.md` | `$env:ENROLLMENT_SERVICE_URL="http://localhost:8083"; $env:TEST_STUDENT_ID="1"; $env:TEST_SECTION_ID="2"; k6 run .\\infra\\k6\\concurrent-enrollment-test.js` | High: turns an existing implementation into a strong course-defense talking point. |
| Add a short terminology page for application failover vs database failover vs backup/restore vs RPO/RTO | This removes the biggest presentation risk: overclaiming HA. | Low | `docs/course-audit/*`; optionally `README.md`; optionally `docs/ha/*` | `rg -n "application-level failover|PostgreSQL failover|backup and restore|RPO|RTO" README.md docs infra` | High: improves accuracy and confidence during Q&A. |

## Nice-to-have before presentation

| Item | Reason | Risk level | Files likely affected | Verification command | Presentation value |
| --- | --- | --- | --- | --- | --- |
| Add a docs-only DCL and least-privilege appendix | The course covers DCL, but the repo currently uses local default credentials and no explicit role/grant examples. | Low | `docs/course-audit/*`; optionally a new `db/` appendix file | `rg -n "GRANT|REVOKE|CREATE ROLE|ALTER ROLE" db docs` | Medium: gives the team a clean answer when security comes up. |
| Add a small SLO and alerting proposal | Prometheus exists, but the course also covers SLOs and alerting; a concise proposal keeps the answer honest without pretending those controls are live. | Low | `docs/course-audit/*`; optionally `infra/prometheus/` | `rg -n "SLO|ALERT|expr:" docs infra` | Medium: shows observability maturity beyond raw metrics. |
| Refresh or annotate stale diagrams and old planning docs | Several early files still say Redis, RabbitMQ, Prometheus, Grafana, or the gateway are only future or planned. | Low | `docs/diagrams/*`; `docs/DOCKER_LOCAL.md`; historical planning docs if scope is reopened | `rg -n "planificado|planeado|futuro|self-scrape actual|solo health check" docs` | Medium: reduces inconsistency between the audit and older repo material. |

## Future production roadmap

| Item | Reason | Risk level | Files likely affected | Verification command | Presentation value |
| --- | --- | --- | --- | --- | --- |
| Add outbox, idempotency keys, and retry or DLQ handling for messaging | Current RabbitMQ flow is real but lightweight; a stronger delivery model would support more serious distributed consistency claims. | Medium | `backend/enrollment-service/*`; `backend/billing-service/*`; `backend/notification/*`; `db/*` | `rg -n "outbox|idempot|DLQ|retry" backend db` | Medium: valuable for roadmap discussion, not required for the current delivery. |
| Add PostgreSQL replication and read-routing | This is the real path from a centralized academic database to true read-scale and database HA. | High | `docker-compose*`; `infra/*`; likely new PostgreSQL config assets | `docker exec -i campusenroll-postgres psql -U campus -d campusenroll -c "SELECT * FROM pg_stat_replication;"` | High for future architecture discussion, but not needed for the final classroom demo. |
| Extend HA beyond `course-service` and define a real entrypoint strategy | The repo demonstrates narrow app failover today; a broader gateway or HAProxy strategy would make the platform story more complete. | High | `docker-compose.ha-demo.yml`; `infra/load-balancer/*`; possibly a real gateway module | `docker compose -f docker-compose.yml -f docker-compose.apps.yml -f docker-compose.ha-demo.yml ps` | Medium: useful future branch, but not the highest-value next move. |

## Do not implement now because risk is too high

| Item | Reason | Risk level | Files likely affected | Verification command | Presentation value |
| --- | --- | --- | --- | --- | --- |
| Automatic PostgreSQL failover with Patroni, repmgr, or pg_auto_failover | This would change the hardest part of the platform and create new failure modes right before presentation. | High | `docker-compose*`; PostgreSQL config; new infra assets | `docker exec -i campusenroll-postgres psql -U campus -d campusenroll -c "SELECT * FROM pg_stat_replication;"` | Low for immediate delivery; much safer as future work. |
| Full Kubernetes or real multi-node infrastructure migration | The repo already has a clear local story in Compose; a platform migration would create more demo risk than value. | High | Most of `infra/`; deployment assets outside the current repo shape | `kubectl get pods -A` | Low for this course segment; too large for the current scope. |
| Partitioning or anti-hotspot redesign of the current schema | The current dataset and traffic shape do not justify schema-level churn, and the risk to existing demos is high. | High | `db/schema.sql`; service persistence assumptions | `docker exec -i campusenroll-postgres psql -U campus -d campusenroll -c "\\d+ enrollments"` | Low for the current academic goal; mention as advanced theory only. |
| Full cross-service saga compensation redesign | The current event flow is intentionally lightweight; a full compensation design would spread risk across multiple services at once. | High | `backend/enrollment-service/*`; `backend/billing-service/*`; `backend/notification/*`; likely new DB assets | `rg -n "compensation|payment-failed|inventory-failed|outbox" backend` | Medium as a roadmap talking point, but too risky to introduce now. |
