# Container Failure Observation

## Purpose

This document explains how to perform a manual container-failure observation scenario for the checkpoint.

The goal is to gather evidence about service behavior before, during, and after a controlled container stop/start event.

Automatic chaos testing is not implemented yet in this segment.

## Recommended Scenario

Choose one business service that is relevant to the validation session, for example:

- `student-service`
- `course-service`
- `enrollment-service`
- `billing-service`

`notification` can also be observed, but it only exposes `GET /health` in the current implemented state.

## Baseline Commands

Check current container status:

```powershell
docker compose ps
```

Review logs for the selected service:

```powershell
docker compose logs <service>
```

Confirm service health before the failure:

```powershell
curl http://localhost:<port>/health
```

Example:

```powershell
curl http://localhost:8083/health
```

## Failure Injection Steps

1. Capture the baseline state.
2. Stop one selected container.
3. Observe service availability and test behavior.
4. Start the container again.
5. Confirm health recovery and collect post-restart evidence.

Stop a service:

```powershell
docker compose stop <service>
```

Example:

```powershell
docker compose stop enrollment-service
```

Check status immediately after stopping it:

```powershell
docker compose ps
```

Collect service logs:

```powershell
docker compose logs <service>
```

Restart the service:

```powershell
docker compose start <service>
```

Check status after restart:

```powershell
docker compose ps
```

Collect logs again:

```powershell
docker compose logs <service>
```

## What To Observe

During the stopped state, capture:

- whether `GET /health` fails or times out as expected
- whether Postman requests fail consistently
- whether k6 smoke or scenario results show the expected service unavailability
- whether other services remain reachable if they are independent in the current implementation

After restart, capture:

- whether `GET /health` returns successfully again
- whether Postman requests recover
- whether k6 smoke checks recover
- whether Docker reports the container as running again

## Evidence To Capture

For the checkpoint review, capture real evidence such as:

- before/after screenshots
- service health response
- Postman result
- k6 result summary
- Docker container status from `docker compose ps`
- relevant `docker compose logs <service>` excerpts

## Suggested Validation Pairings

- Run `infra/k6/smoke-test.js` before stopping the container.
- Stop the selected service with `docker compose stop <service>`.
- Re-run `infra/k6/smoke-test.js` and document the failed health check.
- Start the service again with `docker compose start <service>`.
- Re-run `infra/k6/smoke-test.js` and document recovery.

## Limitations

- This is a manual observation procedure, not automated chaos engineering.
- No automatic restart policy validation is claimed here.
- No synthetic failover or multi-replica recovery is implemented in this checkpoint asset.
