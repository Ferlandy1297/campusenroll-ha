# CampusEnroll HA k6 Assets

## Purpose

This folder contains the practical k6 assets for final technical validation of CampusEnroll HA:

- smoke check for service availability
- exact 50,000-request read scenario
- concurrent enrollment attempt against the duplicate-active rule
- manual container-failure observation guide

These files are prepared assets. They still need to be executed during the final demo to produce real evidence.

## Current Execution Model

- `docker-compose.yml` starts shared infrastructure only: PostgreSQL, Redis, RabbitMQ, Prometheus, and Grafana.
- The Spring Boot services are expected to run separately, typically with `mvn spring-boot:run`.
- Default local URLs used by the scripts:
  - `student-service`: `http://localhost:8081`
  - `course-service`: `http://localhost:8082`
  - `enrollment-service`: `http://localhost:8083`
  - `billing-service`: `http://localhost:8084`
  - `notification`: `http://localhost:8085`

All URLs can be overridden with environment variables.

## Files

- `infra/k6/smoke-test.js`
- `infra/k6/load-50000-requests.js`
- `infra/k6/concurrent-enrollment-test.js`
- `infra/k6/container-failure-observation.md`

## Prerequisites

- shared infra up if the target service depends on it:

```powershell
docker compose up -d postgres redis rabbitmq
docker compose ps
```

- services running on the expected local ports
- `db/schema.sql` and `db/data.sql` loaded if you want deterministic demo data
- for the concurrency script, a valid student and section pair

Database load example:

```powershell
Get-Content -Raw .\db\schema.sql | docker exec -i campusenroll-postgres psql -U campus -d campusenroll -v ON_ERROR_STOP=1
Get-Content -Raw .\db\data.sql | docker exec -i campusenroll-postgres psql -U campus -d campusenroll -v ON_ERROR_STOP=1
```

## Run Locally

If `k6` is installed locally, prefer these commands on Windows PowerShell:

### 1. Smoke Test

```powershell
k6 run .\infra\k6\smoke-test.js
```

Expected output:

- all health checks should return HTTP 200
- `http_req_failed` should stay near `0`
- `checks` should stay near `1.00`

Evidence to capture:

- terminal summary
- one screenshot showing all health checks passing

### 2. Exact 50,000-Request Read Scenario

```powershell
k6 run .\infra\k6\load-50000-requests.js
```

Optional tuning:

```powershell
$env:REQUEST_TARGET="50000"
$env:VUS="100"
$env:MAX_DURATION="10m"
k6 run .\infra\k6\load-50000-requests.js
```

What this script does:

- executes exactly `REQUEST_TARGET` HTTP requests
- rotates across implemented read endpoints
- keeps the scenario non-destructive

Basic pass/fail interpretation:

- pass: total requests reaches 50,000, `http_req_failed` stays low, and p95/p99 remain reviewable
- investigate: high failure rate, repeated 5xx responses, or latency spikes that break your presentation target

Evidence to capture:

- request total from the final k6 summary
- throughput / request rate
- `http_req_failed`
- p95 and p99 latency

### 3. Concurrent Enrollment Attempt

```powershell
$env:ENROLLMENT_SERVICE_URL="http://localhost:8083"
$env:TEST_STUDENT_ID="1"
$env:TEST_SECTION_ID="2"
$env:VUS="20"
$env:ITERATIONS="20"
$env:MAX_DURATION="1m"
k6 run .\infra\k6\concurrent-enrollment-test.js
```

Expected behavior:

- one `201 Created` and the rest `409 Conflict`, or
- all `409 Conflict` if the active enrollment already existed before the test

Basic pass/fail interpretation:

- pass: no duplicate success responses for the same `studentId + sectionId`
- investigate: multiple `201` responses for the same pair, 5xx responses, or missing conflict messages

Evidence to capture:

- k6 summary
- counts for `enrollment_created_responses`
- counts for `enrollment_conflict_responses`
- one sample `409` response body

### 4. Container Failure Observation

Use:

```powershell
Get-Content .\infra\k6\container-failure-observation.md
```

Recommended S18 observation:

- stop `redis`
- keep `course-service` running locally
- repeat `GET /api/courses`
- capture that reads continue with cache fallback

## Docker Fallback

If `k6` is not installed locally, use the Docker image and point requests to `host.docker.internal`.

PowerShell note:

- avoid `docker run ... run - < file.js`; stdin redirection in that form is not reliable in Windows PowerShell
- prefer a mounted volume and a script path inside the container

Smoke test example:

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

Replace `/scripts/smoke-test.js` with the script you want to run.

If you run k6 on the Compose network instead of through published host ports, attach `--network <compose-network>` and replace `host.docker.internal` with the Docker service names.

## Notes

- `load-50000-requests.js` now reaches the configured request target exactly instead of approximating it by duration.
- `concurrent-enrollment-test.js` is intentionally focused on one critical rule and is the best artifact to pair with the enrollment consistency evidence.
- `container-failure-observation.md` is manual by design; it is not an automated chaos suite.
