# Final Regression Checklist - S34

This is the single operational checklist for final validation before the presentation.

## 1. Git clean state

Commands:

```powershell
git status --short
git diff --check
```

Expected:

- `git status --short` returns no output in the presentation branch
- `git diff --check` returns no whitespace errors

## 2. Main Docker stack startup

Commands:

```powershell
docker compose -f docker-compose.yml -f docker-compose.apps.yml -f docker-compose.ha-demo.yml down -v --remove-orphans
docker compose -f docker-compose.yml -f docker-compose.apps.yml -f docker-compose.ha-demo.yml up -d --build
Start-Sleep -Seconds 90
docker compose -f docker-compose.yml -f docker-compose.apps.yml -f docker-compose.ha-demo.yml ps
```

Expected:

- `campusenroll-postgres`, `campusenroll-redis`, `campusenroll-rabbitmq`, `campusenroll-prometheus`, and `campusenroll-grafana` are `Up`
- `student-service`, `course-service`, `course-service-replica`, `enrollment-service`, `billing-service`, `notification`, and `haproxy` are `Up`
- health-enabled containers show `healthy` after startup settles

## 3. Main schema bootstrap verification

Commands:

```powershell
docker exec -i campusenroll-postgres psql -U campus -d campusenroll -c "SELECT table_name FROM information_schema.tables WHERE table_schema='public' ORDER BY table_name;"
docker exec -i campusenroll-postgres psql -U campus -d campusenroll -c "\d billings"
```

Expected:

- the clean startup already loaded `db/schema.sql` and `db/data.sql` through `/docker-entrypoint-initdb.d`
- table list includes `academic_periods`, `billings`, `courses`, `enrollments`, `idempotency_records`, `outbox_events`, `schedule_blocks`, `sections`, and `students`
- `\d billings` succeeds and shows the authoritative billing columns and constraints instead of `relation "billings" does not exist`

## 4. Five service health checks

Commands:

```powershell
curl.exe http://localhost:8081/health
curl.exe http://localhost:8082/health
curl.exe http://localhost:8083/health
curl.exe http://localhost:8084/health
curl.exe http://localhost:8085/health
```

Expected:

- each endpoint returns `HTTP 200`
- each JSON payload contains `status":"UP"` or equivalent service `UP` result

## 5. PostgreSQL connectivity

Commands:

```powershell
docker exec -i campusenroll-postgres psql -U campus -d campusenroll -c "SELECT current_database(), current_user;"
docker exec -i campusenroll-postgres psql -U campus -d campusenroll -c "SELECT COUNT(*) AS students FROM students; SELECT COUNT(*) AS sections FROM sections; SELECT COUNT(*) AS billings FROM billings;"
```

Expected:

- `current_database` is `campusenroll`
- `current_user` is `campus`
- table counts return nonzero seeded values, including `billings`

## 6. Redis cache verification

Commands:

```powershell
docker exec -i campusenroll-redis redis-cli ping
curl.exe http://localhost:8082/api/courses
curl.exe http://localhost:8082/api/courses
docker exec -i campusenroll-redis redis-cli --scan --pattern "courses::*"
```

Expected:

- `PONG`
- both course requests return `HTTP 200`
- at least one `courses::*` key appears after repeated reads

## 7. RabbitMQ queues and bindings

Commands:

```powershell
docker exec -i campusenroll-rabbitmq rabbitmqctl list_queues name messages_ready messages_unacknowledged consumers
docker exec -i campusenroll-rabbitmq rabbitmqctl list_bindings source_name destination_name routing_key
```

Expected:

- queue list includes `notification.events`
- queue list includes `enrollment.compensation.events`
- binding list includes `campusenroll.events -> notification.events`
- binding list includes `campusenroll.events -> enrollment.compensation.events` with `billing.status.changed`

## 8. Prometheus targets

Commands:

```powershell
Invoke-WebRequest -UseBasicParsing http://localhost:9090/-/ready
Invoke-WebRequest -UseBasicParsing http://localhost:9090/api/v1/targets
```

Expected:

- readiness request returns `HTTP 200`
- targets JSON includes `prometheus`, `student-service`, `course-service`, `enrollment-service`, `billing-service`, and `notification`
- active targets should be `up`

## 9. Prometheus alert rules

Commands:

```powershell
Invoke-WebRequest -UseBasicParsing http://localhost:9090/api/v1/rules
```

Expected:

- rules JSON includes the loaded groups for CampusEnroll
- rule names include the implemented availability and HTTP alert coverage

## 10. Idempotency-Key verification

Commands:

```powershell
$enrollmentHeaders = @{
  "Content-Type" = "application/json"
  "Idempotency-Key" = "enrollment-idem-final-1"
}
$enrollmentBody = '{"studentId":1,"sectionId":2}'
$firstEnrollment = Invoke-WebRequest -Method Post -Uri 'http://localhost:8083/api/enrollments' -Headers $enrollmentHeaders -Body $enrollmentBody
$secondEnrollment = Invoke-WebRequest -Method Post -Uri 'http://localhost:8083/api/enrollments' -Headers $enrollmentHeaders -Body $enrollmentBody
$enrollment = $firstEnrollment.Content | ConvertFrom-Json
$firstEnrollment.StatusCode
$secondEnrollment.StatusCode
$firstEnrollment.Content
$secondEnrollment.Content
docker exec -i campusenroll-postgres psql -U campus -d campusenroll -c "SELECT service_name, operation_name, idempotency_key, status, response_status FROM idempotency_records WHERE service_name = 'enrollment-service' ORDER BY id DESC LIMIT 5;"
```

Expected:

- first request returns `201`
- second request with the same key and same body returns `201`
- both response bodies are the same enrollment
- `idempotency_records` stores the `enrollment-idem-final-1` entry

## 11. Transactional outbox verification

Commands:

```powershell
docker exec -i campusenroll-postgres psql -U campus -d campusenroll -c "SELECT id, service_name, aggregate_type, aggregate_id, event_type, routing_key, status, attempts, created_at, published_at FROM outbox_events ORDER BY id DESC LIMIT 20;"
```

Expected:

- recent rows include `EnrollmentCreatedEvent`
- rows use the expected routing keys such as `enrollment.created` and `billing.status.changed`
- healthy publication flow normally shows `status = PUBLISHED`

## 12. Billing idempotency plus setup for compensation

Commands:

```powershell
$billingHeaders = @{
  "Content-Type" = "application/json"
  "Idempotency-Key" = "billing-idem-final-1"
}
$billingBody = (@{
  enrollmentId = $enrollment.id
  amount = 150.75
  currency = 'USD'
  status = 'PENDING'
} | ConvertTo-Json -Compress)
$firstBilling = Invoke-WebRequest -Method Post -Uri 'http://localhost:8084/api/billings' -Headers $billingHeaders -Body $billingBody
$secondBilling = Invoke-WebRequest -Method Post -Uri 'http://localhost:8084/api/billings' -Headers $billingHeaders -Body $billingBody
$billing = $firstBilling.Content | ConvertFrom-Json
$firstBilling.StatusCode
$secondBilling.StatusCode
$firstBilling.Content
$secondBilling.Content
docker exec -i campusenroll-postgres psql -U campus -d campusenroll -c "SELECT service_name, operation_name, idempotency_key, status, response_status FROM idempotency_records WHERE service_name = 'billing-service' ORDER BY id DESC LIMIT 5;"
```

Expected:

- first billing request returns `201`
- second request with the same key and same body returns `201`
- both response bodies are the same billing
- `idempotency_records` stores the `billing-idem-final-1` entry

## 13. Saga compensation verification

Commands:

```powershell
$cancelledBilling = Invoke-RestMethod -Method Patch -Uri "http://localhost:8084/api/billings/$($billing.id)/status" -ContentType 'application/json' -Body (@{
  status = 'CANCELLED'
} | ConvertTo-Json -Compress)
Start-Sleep -Seconds 15
docker exec -i campusenroll-postgres psql -U campus -d campusenroll -c "SELECT id, enrollment_id, status FROM billings WHERE id = $($billing.id);"
docker exec -i campusenroll-postgres psql -U campus -d campusenroll -c "SELECT id, student_id, section_id, status FROM enrollments WHERE id = $($enrollment.id);"
docker compose -f docker-compose.yml -f docker-compose.apps.yml -f docker-compose.ha-demo.yml logs enrollment-service --tail=120
```

Expected:

- the billing row ends as `CANCELLED`
- the related enrollment row ends as `CANCELLED`
- enrollment-service logs include `Processed billing cancellation compensation`

## 14. HAProxy application failover or switchover verification

Commands:

```powershell
curl.exe -i http://localhost:8080/api/courses
curl.exe -i http://localhost:8080/health/course
docker compose -f docker-compose.yml -f docker-compose.apps.yml -f docker-compose.ha-demo.yml stop course-service
curl.exe -i http://localhost:8080/api/courses
docker compose -f docker-compose.yml -f docker-compose.apps.yml -f docker-compose.ha-demo.yml start course-service
curl.exe -i http://localhost:8080/api/courses
```

Expected:

- gateway returns `HTTP 200` before failover
- gateway still returns `HTTP 200` while `course-service` is stopped
- service remains available through `course-service-replica`

## 15. PostgreSQL HA demo S32 verification

Commands:

```powershell
docker compose -f docker-compose.db-ha-demo.yml down -v --remove-orphans
docker compose -f docker-compose.db-ha-demo.yml config
docker compose -f docker-compose.db-ha-demo.yml up -d --build
Start-Sleep -Seconds 45
docker compose -f docker-compose.db-ha-demo.yml ps
docker exec -i campusenroll-pg-primary psql -U campus -d campusenroll_ha_demo -c "SELECT pg_is_in_recovery();"
docker exec -i campusenroll-pg-replica psql -U campus -d campusenroll_ha_demo -c "SELECT pg_is_in_recovery();"
docker exec -i campusenroll-pg-primary psql -U campus -d campusenroll_ha_demo -c "SELECT application_name, state, sync_state FROM pg_stat_replication;"
docker exec -i campusenroll-pg-replica psql -U campus -d campusenroll_ha_demo -c "SELECT status, conninfo FROM pg_stat_wal_receiver;"
docker exec -i campusenroll-pg-primary psql -U campus -d campusenroll_ha_demo -c "SELECT COUNT(*) FROM replication_probe;"
docker exec -i campusenroll-pg-primary psql -U campus -d campusenroll_ha_demo -c "INSERT INTO replication_probe(label) VALUES ('replicated-from-primary');"
docker exec -i campusenroll-pg-replica psql -U campus -d campusenroll_ha_demo -c "SELECT id, label, created_at FROM replication_probe ORDER BY id DESC LIMIT 5;"
docker stop campusenroll-pg-primary
docker exec -u postgres campusenroll-pg-replica pg_ctl -D /var/lib/postgresql/data promote
docker exec -i campusenroll-pg-replica psql -U campus -d campusenroll_ha_demo -c "SELECT pg_is_in_recovery();"
docker exec -i campusenroll-pg-replica psql -U campus -d campusenroll_ha_demo -c "INSERT INTO replication_probe(label) VALUES ('written-after-promotion'); SELECT id, label, created_at FROM replication_probe ORDER BY id DESC LIMIT 5;"
```

Expected:

- checkout keeps `infra/postgres-ha/*.sh` in LF so the Linux containers can execute them reliably
- primary returns `false` for `pg_is_in_recovery()`
- replica returns `true` before promotion
- replication state is normally `streaming`
- `replication_probe` already exists on the primary before the insert
- the primary write appears on the replica
- replica returns `false` after promotion
- promoted replica accepts writes

Cleanup:

```powershell
docker compose -f docker-compose.db-ha-demo.yml down -v --remove-orphans
```

## 16. k6 smoke test

Commands:

```powershell
k6 run .\infra\k6\smoke-test.js
```

Expected:

- health checks return `HTTP 200`
- `http_req_failed` stays near `0`
- `checks` stay near `100%`

## 17. Backup creation

Commands:

```powershell
powershell -ExecutionPolicy Bypass -File infra/backups/backup-postgres.ps1
Get-ChildItem infra/backups/output | Sort-Object LastWriteTime -Desc | Select-Object -First 3 Name, LastWriteTime, Length
```

Expected:

- backup script completes without error
- a new `.dump` file appears near the top of the listing

## 18. DBeaver inspection checklist

Manual checklist:

- connect to `localhost:55432`, database `campusenroll`, user `campus`
- expand schemas and confirm `students`, `courses`, `sections`, `schedule_blocks`, `enrollments`, `billings`, `idempotency_records`, and `outbox_events`
- inspect `enrollments` and confirm `status` values such as `ENROLLED` and `CANCELLED`
- inspect `idempotency_records` and confirm the final demo keys are visible
- inspect `outbox_events` and confirm recent `EnrollmentCreatedEvent` and `BillingStatusChangedEvent` rows

Expected:

- the connection succeeds
- the expected tables are visible
- the business and evidence tables match the demo flow already executed

## 19. Postman inspection checklist

Manual checklist:

1. import `postman/campusenroll-ha.postman_collection.json`
2. import `postman/campusenroll-ha.local.postman_environment.json`
3. run `00 - Health Checks`
4. run `01 - Students`
5. run `02 - Academic Catalog`
6. run `03 - Enrollments`
7. run `04 - Billings`
8. run `05 - Notification`

Expected:

- health checks succeed
- catalog and student requests succeed
- enrollment and billing flows match the validated REST and DB evidence
- notification flow remains observable through logs and RabbitMQ bindings
