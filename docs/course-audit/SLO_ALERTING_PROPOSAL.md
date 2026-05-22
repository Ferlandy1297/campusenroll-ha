# SLO And Alerting Status

## Purpose

This document keeps the S27 academic SLO explanation, but it now records the S28 runtime change:

- Prometheus alert rules are implemented in the repository
- Alertmanager routing is still future work
- Grafana is still available infrastructure, not a production-complete dashboard package

## Key terms

### SLI

Service Level Indicator.

This is the metric that gets measured.

Examples in CampusEnroll HA:

- target availability through `up`
- HTTP 5xx rate through `http_server_requests_seconds_count`
- HTTP latency through `http_server_requests_seconds_bucket`

### SLO

Service Level Objective.

This is the target value expected from the indicator.

Examples:

- availability above `99.0%`
- HTTP 5xx rate below `1%`
- p95 latency below `1.5s`

### SLA

Service Level Agreement.

This is a business promise with consequences.

CampusEnroll HA does not define SLAs in the current academic repo.

### Alerting

Alerting is the rule layer that evaluates metrics and marks abnormal conditions.

CampusEnroll HA now implements alert evaluation inside Prometheus itself.

## Current implemented observability

CampusEnroll HA currently implements:

- `GET /health` endpoints in the five services
- `/actuator/prometheus` endpoints in the five services
- Prometheus scraping in `infra/prometheus/prometheus.yml`
- active rule loading through `rule_files`
- active alert rules in `infra/prometheus/rules/campusenroll-alerts.yml`
- Grafana container infrastructure in `docker-compose.yml`

This means the repo now has:

- metrics
- scrape targets
- live Prometheus rule evaluation

What it does not yet claim:

- Alertmanager routing
- email, Slack, Teams, or pager notifications
- finished Grafana dashboards
- production SLO governance or error-budget process

## Metrics, dashboards, and alerts are different

### Metrics

Raw measurements exposed by the applications.

Current CampusEnroll HA status:

- implemented through Spring Boot Actuator and Micrometer

### Dashboards

Visualizations built on top of metrics.

Current CampusEnroll HA status:

- Grafana infrastructure is available
- finished dashboard packages are not claimed as complete

### Alerts

Rules that evaluate metric conditions and create operational signals.

Current CampusEnroll HA status:

- implemented inside Prometheus
- visible in `/alerts` and `/rules`
- not yet routed externally because Alertmanager is not wired

## Academic SLO set used by the alert rules

### 1. Service availability

Indicator:

- Prometheus `up` per application target

Objective:

- each application target should stay available during the local demo window

Implemented alert:

- `CampusEnrollServiceDown`

### 2. Request error rate

Indicator:

- ratio of HTTP 5xx requests over total business HTTP requests in the last 5 minutes

Objective:

- 5xx rate below `1%`

Implemented alert:

- `CampusEnrollHighHttpErrorRate`

### 3. p95 latency

Indicator:

- p95 latency from `http_server_requests_seconds_bucket`

Objective:

- p95 below `1.5s`

Implemented alert:

- `CampusEnrollHighP95Latency`

### 4. Target completeness

Indicator:

- presence of the expected Prometheus target series

Objective:

- all expected targets should exist in the Prometheus rule evaluation set

Implemented alert:

- `CampusEnrollPrometheusTargetMissing`

## Implemented Prometheus rule file

Active file:

- `infra/prometheus/rules/campusenroll-alerts.yml`

Implemented rules:

- `CampusEnrollServiceDown`
- `CampusEnrollHighHttpErrorRate`
- `CampusEnrollHighP95Latency`
- `CampusEnrollPrometheusTargetMissing`

Important implementation notes:

- the error-rate and latency alerts intentionally exclude `/actuator/prometheus`, `/actuator/health`, `/actuator/info`, and `/health`
- the latency alert required enabling `http.server.requests` histogram buckets in the five service `application.yml` files
- the alert pages work without Alertmanager; Prometheus still shows rule state locally

## What remains future work

The following are still future work:

- Alertmanager wiring
- external notifications
- backup freshness metric export and alerting
- production dashboard packs
- formal error budgets and service governance

Example of a still-future backup alert:

```yaml
time() - campusenroll_backup_last_success_unixtime > 86400
```

That metric does not currently exist in the repo.

## Presentation-safe wording

Use wording like this:

`CampusEnroll HA already exposes real Prometheus metrics and now loads active alert rules for service availability, missing targets, HTTP 5xx rate, and p95 latency. Alertmanager routing and production-complete dashboards are still future work.`

## Validation commands

```powershell
docker compose -f docker-compose.yml -f docker-compose.apps.yml -f docker-compose.ha-demo.yml up -d --build prometheus
Start-Sleep -Seconds 20
curl.exe http://localhost:9090/-/ready
Start-Process "http://localhost:9090/alerts"
Start-Process "http://localhost:9090/rules"
docker compose -f docker-compose.yml -f docker-compose.apps.yml -f docker-compose.ha-demo.yml restart prometheus
docker compose -f docker-compose.yml -f docker-compose.apps.yml -f docker-compose.ha-demo.yml logs prometheus --tail=120
```

Supporting inspection:

```powershell
Get-Content .\infra\prometheus\prometheus.yml
Get-Content .\infra\prometheus\rules\campusenroll-alerts.yml
curl.exe http://localhost:8081/actuator/prometheus
curl.exe http://localhost:8082/actuator/prometheus
Start-Process "http://localhost:9090/targets"
```

## Bottom line

CampusEnroll HA no longer treats alerting as proposal-only.

The correct statement after S28 is:

- Prometheus metrics are implemented
- Prometheus scraping is implemented
- Prometheus alert rules are implemented
- Alertmanager routing is not implemented
- Grafana dashboards are not claimed as production-complete
