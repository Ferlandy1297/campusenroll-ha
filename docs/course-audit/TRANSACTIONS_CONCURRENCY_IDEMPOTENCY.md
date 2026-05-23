# Transactions, Concurrency, And Idempotency

## Purpose

This document explains how CampusEnroll HA currently handles transactional consistency, duplicate protection, and basic concurrency concerns.

It is intentionally honest about the boundary between:

- what is already implemented
- what is partially covered by the current design
- what still belongs to a future production hardening phase

## ACID in this project

ACID in CampusEnroll HA should be interpreted in the context of the current centralized PostgreSQL model and the Spring service layer.

### Atomicity

Write operations in the services are grouped in database transactions through Spring `@Transactional`.

Examples:

- creating or updating enrollments
- creating billings
- updating billing status
- creating catalog records

### Consistency

Consistency is enforced by both application logic and the relational model.

Examples from the current project:

- primary keys and foreign keys
- check constraints on statuses, positive amounts, and trimmed values
- partial unique index for active enrollments
- partial unique index for pending billings

### Isolation

PostgreSQL isolates concurrent transactions so readers and writers do not see invalid intermediate states.

CampusEnroll HA relies on PostgreSQL transactional behavior plus Spring defaults. The repo does not yet define custom isolation settings per use case.

### Durability

Once PostgreSQL commits a transaction, the change becomes part of the persisted database state. In this project, durability is then reinforced operationally by the backup and restore layer under `infra/backups/`.

## How PostgreSQL MVCC helps here

PostgreSQL uses MVCC, or Multi-Version Concurrency Control.

In practical terms for this project, MVCC allows:

- readers to continue without always blocking writers
- writers to commit changes without exposing partial state to other transactions
- concurrent request handling without falling into immediate table-wide locking

This matters for CampusEnroll HA because the current system includes:

- concurrent catalog reads
- enrollment creation attempts that may collide
- billing status updates that must not create invalid duplicate states

The repo does not implement a separate MVCC lab inside CampusEnroll. Instead, it benefits from PostgreSQL MVCC as the underlying transaction engine.

## How Spring `@Transactional` maps to database transactions

The implemented services use Spring transactions as the application boundary around database work.

Representative examples:

- `backend/student-service/src/main/java/com/campusenroll/studentservice/student/StudentService.java`
- `backend/course-service/src/main/java/com/campusenroll/courseservice/catalog/service/CourseService.java`
- `backend/course-service/src/main/java/com/campusenroll/courseservice/catalog/service/AcademicPeriodService.java`
- `backend/course-service/src/main/java/com/campusenroll/courseservice/catalog/service/SectionService.java`
- `backend/enrollment-service/src/main/java/com/campusenroll/enrollmentservice/enrollment/EnrollmentService.java`
- `backend/billing-service/src/main/java/com/campusenroll/billing/billing/BillingService.java`

Typical mapping:

- `@Transactional(readOnly = true)`
  - used for read paths
- `@Transactional`
  - used for create or update paths

Suggested inspection command:

```powershell
rg -n "@Transactional" backend
```

## What isolation levels mean

Isolation levels define what one transaction can observe while another transaction is in progress.

For presentation purposes, keep the explanation simple:

- `READ COMMITTED`
  - each statement sees committed data as of the start of that statement
- `REPEATABLE READ`
  - repeated reads inside the same transaction stay on a stable snapshot
- `SERIALIZABLE`
  - strongest consistency model, but conflicting transactions may be aborted and retried

Current CampusEnroll HA position:

- PostgreSQL provides these capabilities
- the current repo does not explicitly configure them per service method
- the current business logic relies more on relational constraints and transaction ordering than on custom isolation tuning

## What CampusEnroll HA implements today

### 1. Transactional service methods

The implemented services use Spring transaction boundaries for CRUD and status updates.

Why it matters:

- service methods do not leave partially applied writes on success
- error handling can convert relational conflicts into clean API conflicts

### 2. Duplicate active enrollment protection

`EnrollmentService` protects against duplicate active enrollments in two layers:

- application-level pre-check with `existsByStudentIdAndSectionIdAndStatus(...)`
- relational fallback through the partial unique index `uq_enrollments_active_student_section`

The service then calls `saveAndFlush(...)` before enqueueing the transactional outbox event. That ordering still matters because a late database conflict should not create a false-positive event intent.

### 3. Duplicate pending billing protection

`BillingService` protects against more than one active `PENDING` billing for the same enrollment in two layers:

- application-level repository checks
- relational fallback through `uq_billings_pending_enrollment`

### 4. Partial unique indexes as concurrency guards

The current schema uses business-focused partial uniqueness:

- `uq_enrollments_active_student_section`
  - only one active `ENROLLED` row per `student_id + section_id`
- `uq_billings_pending_enrollment`
  - only one active `PENDING` billing per `enrollment_id`

These are strong Database II aligned artifacts because they enforce rules directly at the relational layer.

### 5. HTTP `Idempotency-Key` for selected critical writes

S29 adds real API-level idempotency for these write endpoints:

- `POST /api/enrollments`
- `POST /api/billings`

Implemented behavior:

- if `Idempotency-Key` is absent, existing behavior is preserved
- if the same key is reused with the same payload after a successful first request, the service replays the stored HTTP status and JSON response body
- if the same key is reused with a different payload, the service returns `409 Conflict`
- the replayed response is backed by a PostgreSQL table named `idempotency_records`
- the uniqueness guard is `(service_name, operation_name, idempotency_key)`

Why it matters:

- client retries do not create duplicate enrollment rows
- client retries do not create duplicate billing rows
- replayed enrollment requests do not create duplicate outbox rows or republish `EnrollmentCreatedEvent`
- the solution stays aligned with Database II because the concurrency guard is still enforced by PostgreSQL

### 6. Transactional outbox for RabbitMQ reliability

S30 adds a real transactional outbox for the event-producing services:

- enrollment creation writes the enrollment row and an `outbox_events` row atomically
- billing status changes write the billing update and an `outbox_events` row atomically
- a scheduled publisher in each producer service reads pending outbox rows and publishes them to RabbitMQ
- after a successful publish, the row is marked `PUBLISHED`
- on failure, the row records `attempts` and `last_error`
- after a small retry threshold, the row is marked `FAILED`

This improves reliability because the business write and the event intent are now persisted together in PostgreSQL before RabbitMQ delivery.

S31 keeps that producer-side mechanism intact and reuses the published billing status events as the reliable source for downstream compensation.

It does not mean:

- exactly-once distributed delivery
- a full saga engine with centralized orchestration, DLQ policies, or advanced retry choreography
- a transactional broker plus database two-phase commit

The correct phrase is:

`CampusEnroll HA now implements a transactional outbox for the enrollment and billing producers. That improves event reliability and now feeds the S31 compensation flow, but it is not the same as a full saga engine or exactly-once distributed delivery.`

### 7. Basic saga compensation through RabbitMQ choreography

S31 adds a narrow compensation path on top of the existing RabbitMQ choreography and S30 outbox.

Current behavior:

- `billing-service` still publishes `BillingStatusChangedEvent` only when the billing status actually changes
- the event payload already includes `enrollmentId`
- `enrollment-service` now binds `enrollment.compensation.events` to `billing.status.changed`
- when the new billing status is `CANCELLED`, `enrollment-service` looks up the related enrollment and changes it from `ENROLLED` to `CANCELLED`
- if the enrollment is already `CANCELLED`, the compensation step becomes a no-op
- if the enrollment does not exist, the listener logs safely and returns without crashing
- if the billing status is `PAID` or `PENDING`, the listener ignores the event

Why this is idempotent enough for the current scope:

- duplicate `billing.status.changed` events with `newStatus=CANCELLED` do not keep mutating the row
- after the first successful compensation, the enrollment is already `CANCELLED`
- the next duplicate event becomes a harmless no-op

### 7. k6 concurrent enrollment evidence

The repo includes a focused concurrency test:

- `infra/k6/concurrent-enrollment-test.js`

Its expectation is honest:

- one `201` plus multiple `409` responses, or
- all `409` responses if the active enrollment already existed before the test

That makes it the clearest practical evidence asset for the duplicate-active rule.

## What CampusEnroll HA does not implement

The current repo does not implement:

- explicit `SKIP LOCKED`
- explicit `FOR UPDATE` locking
- a deadlock retry framework
- full idempotency coverage for every write endpoint
- a full saga engine with orchestration, DLQ routing, and broad compensation policies
- seat inventory reservation logic
- automatic replay-safe exactly-once event delivery across services

These gaps should be presented as future production hardening work, not as missing fundamentals for the current academic delivery.

## Anti-oversell: the honest explanation

This is the safest way to explain anti-oversell in CampusEnroll HA:

Implemented now:

- protection against duplicate active enrollment for the same student and section
- a dedicated concurrency test to validate that rule
- relational enforcement through a partial unique index

Not implemented now:

- a full seat inventory anti-oversell workflow
- decrementing a section seat counter inside a reservation transaction
- temporary reservation expiration logic
- queue-based allocation with `SKIP LOCKED`

So the correct phrase is:

`CampusEnroll HA implements duplicate-active enrollment protection, which is a real concurrency control measure, but it does not yet implement a full seat-inventory anti-oversell workflow.`

## Idempotency: the honest explanation

Current idempotency-related strengths:

- `Idempotency-Key` is implemented for `POST /api/enrollments`
- `Idempotency-Key` is implemented for `POST /api/billings`
- repeated successful same-key requests replay the stored response instead of duplicating the business operation
- duplicate-active enrollment protection still exists as a relational fallback
- duplicate-pending billing protection still exists as a relational fallback
- `idempotency_records` stores the request hash, response status, response body, and completion state

Current idempotency limitations:

- not every write endpoint in the platform uses idempotency keys yet
- only selected critical endpoints currently expose `Idempotency-Key`
- the transactional outbox is now implemented only for the current event-producing services
- only a basic compensation path is implemented; broader saga recovery and DLQ handling remain future work
- the RabbitMQ flow is still not a fully replay-safe exactly-once distributed pipeline

So the correct phrase is:

`CampusEnroll HA now implements real API idempotency keys for selected critical write endpoints, a transactional outbox for the current event-producing services, and a basic compensation path for cancelled billings, but it still does not implement a full saga engine.`

## Relationship to RabbitMQ choreography

The messaging flow is real, but limited.

Current behavior:

- enrollment writes `EnrollmentCreatedEvent` into `outbox_events`, then a scheduled publisher sends it to RabbitMQ
- billing writes `BillingStatusChangedEvent` into `outbox_events`, then a scheduled publisher sends it to RabbitMQ
- enrollment now consumes `billing.status.changed` on `enrollment.compensation.events` and compensates the related enrollment when the billing status becomes `CANCELLED`
- notification still consumes both published events and records evidence

Current limit:

- there is no central orchestrator, DLQ policy, or full production saga engine
- there is no exactly-once end-to-end delivery guarantee across PostgreSQL and RabbitMQ

That means the current system is better described as lightweight event choreography with basic compensation than as a complete saga platform.

## Useful inspection commands

Inspect the most relevant indexes:

```powershell
docker exec -i campusenroll-postgres psql -U campus -d campusenroll -c "SELECT tablename, indexname, indexdef FROM pg_indexes WHERE schemaname = 'public' AND tablename IN ('enrollments','billings') ORDER BY tablename, indexname;"
```

Inspect the table and index shape directly:

```powershell
docker exec -i campusenroll-postgres psql -U campus -d campusenroll -c "\d enrollments"
docker exec -i campusenroll-postgres psql -U campus -d campusenroll -c "\d billings"
docker exec -i campusenroll-postgres psql -U campus -d campusenroll -c "\d idempotency_records"
docker exec -i campusenroll-postgres psql -U campus -d campusenroll -c "\d outbox_events"
```

Inspect the stored idempotency records:

```powershell
docker exec -i campusenroll-postgres psql -U campus -d campusenroll -c "SELECT id, service_name, operation_name, idempotency_key, status, response_status, created_at, completed_at FROM idempotency_records ORDER BY id;"
docker exec -i campusenroll-postgres psql -U campus -d campusenroll -c "SELECT id, service_name, event_type, routing_key, status, attempts, created_at, published_at FROM outbox_events ORDER BY id DESC LIMIT 20;"
```

Inspect the compensation queue and bindings:

```powershell
docker exec -i campusenroll-rabbitmq rabbitmqctl list_queues name messages_ready messages_unacknowledged consumers
docker exec -i campusenroll-rabbitmq rabbitmqctl list_bindings source_name destination_name routing_key
```

Inspect the compensated enrollments:

```powershell
docker exec -i campusenroll-postgres psql -U campus -d campusenroll -c "SELECT id, student_id, section_id, status FROM enrollments ORDER BY id DESC LIMIT 10;"
```

Run the concurrent enrollment evidence scenario:

```powershell
$env:ENROLLMENT_SERVICE_URL="http://localhost:8083"
$env:TEST_STUDENT_ID="1"
$env:TEST_SECTION_ID="2"
$env:VUS="20"
$env:ITERATIONS="20"
$env:MAX_DURATION="1m"
k6 run .\infra\k6\concurrent-enrollment-test.js
```

Expected interpretation:

- success means no duplicate `201` responses for the same pair
- repeated `409` responses are acceptable and expected after the first success

Run the S29 enrollment idempotency scenario:

```powershell
$headers = @{
  "Content-Type" = "application/json"
  "Idempotency-Key" = "enrollment-idem-demo-1"
}
$body = '{"studentId":1,"sectionId":2}'
$first = Invoke-WebRequest -Method Post -Uri "http://localhost:8083/api/enrollments" -Headers $headers -Body $body
$second = Invoke-WebRequest -Method Post -Uri "http://localhost:8083/api/enrollments" -Headers $headers -Body $body
$first.StatusCode
$second.StatusCode
$first.Content
$second.Content
$differentEnrollmentBody = (@{
  studentId = 1
  sectionId = 3
} | ConvertTo-Json -Compress)
curl.exe -i -X POST http://localhost:8083/api/enrollments -H "Content-Type: application/json" -H "Idempotency-Key: enrollment-idem-demo-1" -d $differentEnrollmentBody
docker exec -i campusenroll-postgres psql -U campus -d campusenroll -c "SELECT id, student_id, section_id, status FROM enrollments WHERE student_id = 1 AND section_id = 2 ORDER BY id;"
```

Run the S29 billing idempotency scenario after the enrollment response above:

```powershell
$enrollment = $first.Content | ConvertFrom-Json
$billingHeaders = @{
  "Content-Type" = "application/json"
  "Idempotency-Key" = "billing-idem-demo-1"
}
$billingBody = (@{
  enrollmentId = $enrollment.id
  amount = 150.75
  currency = "USD"
  status = "PENDING"
} | ConvertTo-Json -Compress)
$billingFirst = Invoke-WebRequest -Method Post -Uri "http://localhost:8084/api/billings" -Headers $billingHeaders -Body $billingBody
$billingSecond = Invoke-WebRequest -Method Post -Uri "http://localhost:8084/api/billings" -Headers $billingHeaders -Body $billingBody
$billingFirst.StatusCode
$billingSecond.StatusCode
$billingFirst.Content
$billingSecond.Content
$differentBillingBody = (@{
  enrollmentId = $enrollment.id
  amount = 175.00
  currency = "USD"
  status = "PENDING"
} | ConvertTo-Json -Compress)
curl.exe -i -X POST http://localhost:8084/api/billings -H "Content-Type: application/json" -H "Idempotency-Key: billing-idem-demo-1" -d $differentBillingBody
docker exec -i campusenroll-postgres psql -U campus -d campusenroll -c "SELECT id, enrollment_id, amount, currency, status FROM billings WHERE enrollment_id = $($enrollment.id) ORDER BY id;"
```

## Presentation-safe wording

Use wording like this:

`CampusEnroll HA already uses real PostgreSQL transactions and relational constraints to protect critical operations. In S29 it adds real Idempotency-Key support for POST /api/enrollments and POST /api/billings, backed by a PostgreSQL idempotency_records table. In S30 it adds a transactional outbox for the enrollment and billing producers, so business writes and event intents are persisted atomically before RabbitMQ delivery. In S31 it adds a basic compensation flow where a cancelled billing event can cancel the related enrollment through RabbitMQ choreography. That is stronger than duplicate-only protection, but it is still not a full seat-capacity anti-oversell system or a full saga engine.`

## Bottom line

CampusEnroll HA is already defensible in Database II terms because it combines:

- Spring transactions
- PostgreSQL MVCC and constraint enforcement
- concurrency-aware duplicate protection
- selected API idempotency keys backed by PostgreSQL
- transactional outbox persistence for producer events
- basic compensation for cancelled billings through RabbitMQ choreography
- a dedicated k6 concurrency asset

The missing pieces are advanced production refinements, not proof that the current system lacks transactional discipline.
