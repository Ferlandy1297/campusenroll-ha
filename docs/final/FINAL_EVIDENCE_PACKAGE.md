# Final Evidence Package - S34

This file lists the screenshots, terminal captures, and saved outputs that should exist before the final presentation.

## 1. Infrastructure and container health

- capture `docker compose -f docker-compose.yml -f docker-compose.apps.yml -f docker-compose.ha-demo.yml ps`
- make sure the image shows the five business services, `course-service-replica`, `haproxy`, and the shared infrastructure as `Up`
- if possible, include the `healthy` state in the same capture

## 2. Service health endpoints

- capture the terminal output of:
  - `curl.exe http://localhost:8081/health`
  - `curl.exe http://localhost:8082/health`
  - `curl.exe http://localhost:8083/health`
  - `curl.exe http://localhost:8084/health`
  - `curl.exe http://localhost:8085/health`
- make sure the `UP` status is visible for all five

## 3. DBeaver schema and tables

- capture the DBeaver tree showing the `campusenroll` connection
- capture the table list after a clean Docker startup so it is clear the main stack loaded the authoritative `db/schema.sql`
- include the visible list of key tables:
  - `students`
  - `courses`
  - `sections`
  - `schedule_blocks`
  - `enrollments`
  - `billings`
  - `idempotency_records`
  - `outbox_events`
- if possible, also capture `\d billings` or the DBeaver table definition to prove `billings` exists without manual post-start SQL loading

## 4. RabbitMQ exchange, queues, and bindings

- capture RabbitMQ Management UI showing the exchange used for CampusEnroll events
- capture the queues page showing `notification.events`
- capture the queues page showing `enrollment.compensation.events`
- capture the bindings page showing `billing.status.changed` bound to the compensation queue

## 5. RabbitMQ event logs or service logs

- capture `billing-service` publishing `BillingStatusChangedEvent`
- capture `enrollment-service` logging `Processed billing cancellation compensation`
- capture `notification` receiving enrollment or billing events

## 6. Prometheus targets

- capture `http://localhost:9090/targets`
- make sure the visible targets include:
  - `prometheus`
  - `student-service`
  - `course-service`
  - `enrollment-service`
  - `billing-service`
  - `notification`

## 7. Prometheus alerts page

- capture `http://localhost:9090/alerts`
- capture `http://localhost:9090/rules`
- make sure the active rule groups for CampusEnroll are visible

## 8. Grafana home or dashboard page

- capture `http://localhost:3000`
- it is enough to show that Grafana is reachable
- do not overclaim finished business dashboards if they are not actually present

## 9. k6 smoke result

- capture the terminal summary of `k6 run .\infra\k6\smoke-test.js`
- make sure `http_req_failed` and `checks` are visible

## 10. k6 load or concurrency result

- capture the terminal summary of:
  - `k6 run .\infra\k6\load-50000-requests.js`
  - or `k6 run .\infra\k6\concurrent-enrollment-test.js`
- preferred visible values:
  - total requests
  - throughput
  - `http_req_failed`
  - p95
  - p99

## 11. Backup output

- capture the terminal output of `infra/backups/backup-postgres.ps1`
- capture `Get-ChildItem infra/backups/output`
- make sure the newest `.dump` file is visible

## 12. HAProxy stats page

- capture `http://localhost:8404/stats`
- if possible, show the failover state while `course-service` is stopped

## 13. PostgreSQL primary and replica verification

- capture the output of:
  - `SELECT pg_is_in_recovery();` on `campusenroll-pg-primary`
  - `SELECT pg_is_in_recovery();` on `campusenroll-pg-replica`
  - `SELECT application_name, state, sync_state FROM pg_stat_replication;`
  - `SELECT status, conninfo FROM pg_stat_wal_receiver;`
- make sure the port separation is clear in the narrative:
  - main stack PostgreSQL: `55432`
  - isolated S32 demo primary: `56432`
  - isolated S32 demo replica: `56433`

## 14. PostgreSQL failover promotion output

- capture the terminal output of:
  - `docker stop campusenroll-pg-primary`
  - `docker exec -u postgres campusenroll-pg-replica pg_ctl -D /var/lib/postgresql/data promote`
  - `SELECT pg_is_in_recovery();` after promotion
  - the successful write to `replication_probe` after promotion

## 15. idempotency_records table

- capture a `psql` or DBeaver view of `idempotency_records`
- make sure the final demo keys and response statuses are visible

## 16. outbox_events table

- capture a `psql` or DBeaver view of `outbox_events`
- include `event_type`, `routing_key`, `status`, `attempts`, and `published_at`

## 17. Saga compensation result

- capture the `billings` row showing `CANCELLED`
- capture the related `enrollments` row showing `CANCELLED`
- capture the enrollment-service compensation log line if possible

## 18. Final git log showing S28 to S32 history

- capture:

```powershell
git log --oneline --decorate --graph -n 25
```

- if the branch uses merge commits, make sure the S28 to S32 merges are visible
- if the branch uses direct commits instead of merge commits, capture the contiguous history entries that correspond to S28, S29, S30, S31, and S32

## 19. Suggested evidence naming

- `E01-repo-overview.png`
- `E02-compose-ps.png`
- `E03-health-checks.png`
- `E04-dbeaver-schema.png`
- `E05-rabbitmq-bindings.png`
- `E06-prometheus-targets.png`
- `E07-prometheus-alerts.png`
- `E08-grafana-home.png`
- `E09-k6-smoke.png`
- `E10-k6-load-or-concurrency.png`
- `E11-backup-output.png`
- `E12-haproxy-stats.png`
- `E13-postgres-replication.png`
- `E14-postgres-promotion.png`
- `E15-idempotency-records.png`
- `E16-outbox-events.png`
- `E17-saga-compensation.png`
- `E18-git-log.png`
