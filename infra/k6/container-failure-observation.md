# Container Failure Observation

## Purpose

This document explains how to perform a manual container-failure observation scenario for the checkpoint.

The goal is to gather evidence about service behavior before, during, and after a controlled container stop/start event.

Automatic chaos testing is not implemented yet in this segment.

## Current Repo Reality

`docker-compose.yml` in this repository currently starts shared infrastructure only:

- `postgres`
- `redis`
- `rabbitmq`
- `prometheus`
- `grafana`

The Spring Boot business services are normally started separately with `mvn spring-boot:run`. Because of that, the most relevant S18 failure observation is `redis` while `course-service` keeps running locally.

## Recommended Scenario For S18

Observe Redis degradation against the new catalog cache in `course-service`.

### Baseline

Start infrastructure and the service:

```powershell
docker compose up -d postgres redis
cd .\backend\course-service
mvn spring-boot:run
```

Warm the cache and capture Redis key evidence:

```powershell
curl.exe http://localhost:8082/api/courses
curl.exe http://localhost:8082/api/courses
docker exec -i campusenroll-redis redis-cli --scan --pattern "courses::*"
```

Optional short read validation:

```powershell
k6 run .\infra\k6\load-50000-requests.js
```

### Failure Injection

```powershell
docker compose stop redis
docker compose ps
```

While Redis is stopped, confirm the endpoint still answers and record logs:

```powershell
curl.exe http://localhost:8082/api/courses
docker compose logs redis
```

Capture the `course-service` console warning that the cache operation failed and the request continued with database access.

### Recovery

```powershell
docker compose start redis
docker compose ps
docker exec -i campusenroll-redis redis-cli ping
curl.exe http://localhost:8082/api/courses
docker exec -i campusenroll-redis redis-cli --scan --pattern "courses::*"
```

## Alternative Scenario

If you want a failure observation closer to the critical enrollment path, stop `postgres` or `rabbitmq` while `enrollment-service` is running locally and record the effect on:

- `POST /api/enrollments`
- event publication logging
- `infra/k6/concurrent-enrollment-test.js`

## What To Observe

During the stopped state, capture:

- whether catalog reads still work when only Redis is stopped
- whether the application logs clearly show cache fallback behavior
- whether k6 read/load results degrade gracefully instead of failing hard
- whether the stopped infrastructure component reports the expected Docker status

After restart, capture:

- whether Redis responds to `PING` again
- whether cache keys reappear after the next catalog request
- whether Docker reports the container as running again

## Evidence To Capture

For the checkpoint review, capture real evidence such as:

- before/after screenshots
- repeated `GET /api/courses` output
- Redis `--scan` output before stop and after restart
- k6 result summary if you ran one
- Docker container status from `docker compose ps`
- relevant `docker compose logs redis` excerpts
- relevant `course-service` warning log excerpt showing cache fallback

## Suggested Validation Pairings

- Run `curl.exe http://localhost:8082/api/courses` twice before stopping Redis.
- Stop Redis with `docker compose stop redis`.
- Re-run `curl.exe http://localhost:8082/api/courses` and document successful fallback.
- Start Redis again with `docker compose start redis`.
- Re-run `curl.exe http://localhost:8082/api/courses` and document cache recovery.

## Limitations

- This is a manual observation procedure, not automated chaos engineering.
- No automatic restart policy validation is claimed here.
- No synthetic failover or multi-replica recovery is implemented in this checkpoint asset.
- Because business services are not defined as Compose services in this repo, stopping a service container is only applicable if you packaged and launched one separately.
