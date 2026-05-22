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

The service then calls `saveAndFlush(...)` before publishing the RabbitMQ event. That ordering matters because a late database conflict should not emit a false-positive enrollment event.

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

### 5. k6 concurrent enrollment evidence

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
- request-level idempotency keys
- the outbox pattern
- full saga compensation
- seat inventory reservation logic
- automatic retry semantics for failed event publication

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

- duplicate-active enrollment protection limits replay damage on the same student-section pair
- duplicate-pending billing protection limits replay damage on the same enrollment
- enrollment event publication happens only after `saveAndFlush(...)`
- billing status change events are only published when the status actually changes

Current idempotency limitations:

- no request id or idempotency key header
- no deduplication table
- no durable outbox
- no guaranteed replay-safe multi-service delivery pipeline

So the correct phrase is:

`CampusEnroll HA includes partial idempotency protection through relational uniqueness and careful event ordering, but it does not implement full API idempotency keys or an outbox pattern.`

## Relationship to RabbitMQ choreography

The messaging flow is real, but limited.

Current behavior:

- enrollment publishes `EnrollmentCreatedEvent`
- billing publishes `BillingStatusChangedEvent`
- notification consumes both and records evidence

Current limit:

- there is no full distributed saga with compensation, retries, and durable outbox semantics

That means the current system is better described as lightweight event choreography than as a complete saga platform.

## Useful inspection commands

Inspect the most relevant indexes:

```powershell
docker exec -i campusenroll-postgres psql -U campus -d campusenroll -c "SELECT tablename, indexname, indexdef FROM pg_indexes WHERE schemaname = 'public' AND tablename IN ('enrollments','billings') ORDER BY tablename, indexname;"
```

Inspect the table and index shape directly:

```powershell
docker exec -i campusenroll-postgres psql -U campus -d campusenroll -c "\d enrollments"
docker exec -i campusenroll-postgres psql -U campus -d campusenroll -c "\d billings"
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

## Presentation-safe wording

Use wording like this:

`CampusEnroll HA already uses real PostgreSQL transactions and relational constraints to protect critical operations. For enrollments, the service checks for an existing active row, flushes the insert, and still relies on a PostgreSQL partial unique index as the final concurrency guard. That is strong duplicate-active protection, but it is not yet a full seat-capacity anti-oversell system and it does not yet use explicit SKIP LOCKED, FOR UPDATE, or idempotency keys.`

## Bottom line

CampusEnroll HA is already defensible in Database II terms because it combines:

- Spring transactions
- PostgreSQL MVCC and constraint enforcement
- concurrency-aware duplicate protection
- a dedicated k6 concurrency asset

The missing pieces are advanced production refinements, not proof that the current system lacks transactional discipline.
