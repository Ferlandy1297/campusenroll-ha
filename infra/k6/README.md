# CampusEnroll HA k6 Assets

## Purpose

This directory contains the initial k6 load-testing assets required for the `Revisión Técnica Avanzada del Proyecto Final` checkpoint.

The goal of these assets is to provide:

- a lightweight smoke check for service availability
- an initial 50,000-request read-focused load scenario
- a concurrency scenario for duplicate active enrollment protection
- a manual observation guide for container failure during validation

These files are checkpoint assets only. They are intended to be executed during final validation; this repository does not claim that the tests have already been run.

## Service URLs Used

Default local URLs used by the scripts:

- `student-service`: `http://localhost:8081`
- `course-service`: `http://localhost:8082`
- `enrollment-service`: `http://localhost:8083`
- `billing-service`: `http://localhost:8084`
- `notification`: `http://localhost:8085`

These defaults can be overridden with environment variables at runtime.

## Files

- `infra/k6/smoke-test.js`
- `infra/k6/load-50000-requests.js`
- `infra/k6/concurrent-enrollment-test.js`
- `infra/k6/container-failure-observation.md`

## Prerequisites

- Docker Compose environment running locally
- CampusEnroll HA services available on the expected ports
- Existing demo data loaded for list/read endpoints
- For `concurrent-enrollment-test.js`, an existing student record and an existing section record

Suggested validation before running k6:

```powershell
docker compose ps
```

## Run k6 With Docker

If k6 is not installed locally, run it with the official Docker image:

```powershell
docker run --rm -i `
  --network host `
  -v "${PWD}/infra/k6:/scripts" `
  grafana/k6 run /scripts/smoke-test.js
```

If `--network host` is not supported on your Docker setup, replace service URLs with `host.docker.internal`, for example:

```powershell
docker run --rm -i `
  -e STUDENT_SERVICE_URL=http://host.docker.internal:8081 `
  -e COURSE_SERVICE_URL=http://host.docker.internal:8082 `
  -e ENROLLMENT_SERVICE_URL=http://host.docker.internal:8083 `
  -e BILLING_SERVICE_URL=http://host.docker.internal:8084 `
  -e NOTIFICATION_SERVICE_URL=http://host.docker.internal:8085 `
  -v "${PWD}/infra/k6:/scripts" `
  grafana/k6 run /scripts/smoke-test.js
```

## How To Run Each Script

### 1. Smoke Test

Purpose: confirm that all current service health endpoints respond correctly.

```powershell
docker run --rm -i `
  --network host `
  -v "${PWD}/infra/k6:/scripts" `
  grafana/k6 run /scripts/smoke-test.js
```

Optional overrides:

```powershell
docker run --rm -i `
  --network host `
  -e STUDENT_SERVICE_URL=http://localhost:8081 `
  -e COURSE_SERVICE_URL=http://localhost:8082 `
  -e ENROLLMENT_SERVICE_URL=http://localhost:8083 `
  -e BILLING_SERVICE_URL=http://localhost:8084 `
  -e NOTIFICATION_SERVICE_URL=http://localhost:8085 `
  -v "${PWD}/infra/k6:/scripts" `
  grafana/k6 run /scripts/smoke-test.js
```

### 2. 50,000-Request Load Scenario

Purpose: exercise the implemented read endpoints with a non-destructive accumulated volume target.

```powershell
docker run --rm -i `
  --network host `
  -v "${PWD}/infra/k6:/scripts" `
  grafana/k6 run /scripts/load-50000-requests.js
```

Optional tuning:

```powershell
docker run --rm -i `
  --network host `
  -e REQUEST_TARGET=50000 `
  -e VUS=100 `
  -e DURATION=5m `
  -v "${PWD}/infra/k6:/scripts" `
  grafana/k6 run /scripts/load-50000-requests.js
```

### 3. Concurrent Enrollment Scenario

Purpose: validate the critical duplicate-active-enrollment rule under parallel requests.

Important:

- this script expects an existing student and an existing section
- this script uses `POST /api/enrollments`
- this script is intentionally focused on one critical business rule, not on Redis, RabbitMQ, or API Gateway behavior

```powershell
docker run --rm -i `
  --network host `
  -e ENROLLMENT_SERVICE_URL=http://localhost:8083 `
  -e TEST_STUDENT_ID=1 `
  -e TEST_SECTION_ID=1 `
  -v "${PWD}/infra/k6:/scripts" `
  grafana/k6 run /scripts/concurrent-enrollment-test.js
```

## Expected Metrics

The checkpoint expects evidence from the following k6 outputs:

- total request count
- request rate / throughput
- `http_req_failed`
- `http_req_duration`
- `checks`
- p95 and p99 latency from the k6 summary

For the concurrency scenario, also review:

- count of `201 Created`
- count of `409 Conflict`
- whether responses are consistent with the duplicate-enrollment rule

## Limitations

- These are initial checkpoint assets, not a full performance engineering suite.
- The load scenario prioritizes implemented `GET` endpoints and avoids destructive writes by default.
- The concurrency script depends on valid existing IDs and may return all `409` responses if an active enrollment already exists before the test starts.
- Automatic chaos testing is not implemented yet; container failure is documented as a manual observation procedure only.
- Prometheus, Grafana, Redis, and RabbitMQ are present in infrastructure, but these scripts do not require them to be integrated into business logic.

## Checkpoint Note

These assets are aligned to the currently implemented CampusEnroll HA endpoints and are intended to support the technical checkpoint review. They must be executed during final validation to produce real evidence and metrics.
