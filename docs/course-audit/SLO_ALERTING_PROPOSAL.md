# SLO And Alerting Proposal

## Purpose

This document closes the S26 observability gap by adding a presentation-ready explanation of:

- SLI
- SLO
- SLA
- alerting

It also proposes a small academic SLO set for CampusEnroll HA without claiming that dashboards or alert rules are already active in production form.

## Key terms

### SLI

Service Level Indicator.

This is the metric you measure.

Examples:

- request success rate
- p95 latency
- uptime of a service target
- age of the latest successful backup

### SLO

Service Level Objective.

This is the target value you want the indicator to meet.

Examples:

- availability above 99%
- error rate below 1%
- p95 latency below 1.5 seconds

### SLA

Service Level Agreement.

This is a formal promise between provider and consumer, usually with business consequences.

CampusEnroll HA does not define SLAs in the current academic repo.

### Alerting

Alerting is the mechanism that turns a metric condition into an action signal.

Examples:

- service down
- sustained high 5xx error rate
- latency degradation
- stale backup evidence

## Current implemented observability

CampusEnroll HA already implements real observability foundations:

- `GET /health` endpoints in the five services
- `/actuator/prometheus` endpoints in the five services
- Prometheus scrape configuration in `infra/prometheus/prometheus.yml`
- Prometheus container in `docker-compose.yml`
- Grafana container in `docker-compose.yml`

This means the project already has:

- metrics
- scrape targets
- reachable observability infrastructure

What it does not yet claim:

- finished dashboards
- active alert rules
- Alertmanager wiring
- production SLO governance

## Metrics, dashboards, and alerts are different

This distinction is important in the final presentation.

### Metrics

Raw measurements exposed by the application or system.

Current CampusEnroll HA status:

- implemented through Spring Boot Actuator and Micrometer Prometheus endpoints

### Dashboards

Visual displays built on top of metrics.

Current CampusEnroll HA status:

- Grafana infrastructure is available
- finished dashboard packages are not claimed as complete

### Alerts

Automated conditions that trigger attention when an SLO or threshold is violated.

Current CampusEnroll HA status:

- proposed in this document
- not claimed as active unless a later segment explicitly wires them

## Proposed academic SLOs

These are reasonable presentation-grade objectives for the current repo shape.

### 1. Service availability

Proposed SLI:

- Prometheus `up` status for each application target

Proposed SLO:

- each application target should be available at least `99.0%` during the local demo window

Why it fits:

- simple
- directly aligned with `/health` plus Prometheus targets

### 2. Request error rate

Proposed SLI:

- proportion of failed HTTP requests over a rolling window

Proposed SLO:

- HTTP 5xx rate below `1%` in the observation window

Why it fits:

- aligns with both Prometheus metrics and k6 evidence

### 3. p95 latency

Proposed SLI:

- p95 of HTTP server request duration

Proposed SLO:

- p95 latency below `1.5s` for the observed endpoints in the local demo scenario

Why it fits:

- aligns with the current k6 thresholds and the project presentation goals

### 4. Backup freshness

Proposed SLI:

- age of the latest successful PostgreSQL backup

Proposed SLO:

- latest successful backup not older than `24h` before the final demo or checkpoint review

Why it fits:

- directly connected to the backup and restore story already implemented in the repo

Important limitation:

- this SLI would require a backup timestamp metric or external export mechanism if the team later wants automated alerting

## Illustrative Prometheus alert rules only

The rules below are examples only.

They are not wired as active alerts by this repository in S27.

## Example 1: service down

```yaml
groups:
  - name: campusenroll-availability
    rules:
      - alert: CampusEnrollServiceDown
        expr: up{job=~"student-service|course-service|enrollment-service|billing-service|notification"} == 0
        for: 2m
        labels:
          severity: warning
        annotations:
          summary: "CampusEnroll service target is down"
          description: "A Prometheus target for a CampusEnroll application has been down for more than 2 minutes."
```

## Example 2: high HTTP error rate

```yaml
groups:
  - name: campusenroll-http-errors
    rules:
      - alert: CampusEnrollHighHttpErrorRate
        expr: |
          (
            sum by (application) (rate(http_server_requests_seconds_count{status=~"5.."}[5m]))
            /
            sum by (application) (rate(http_server_requests_seconds_count[5m]))
          ) > 0.01
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High HTTP 5xx rate"
          description: "A CampusEnroll service is above the proposed 1 percent 5xx error-rate objective."
```

## Example 3: high p95 latency

```yaml
groups:
  - name: campusenroll-latency
    rules:
      - alert: CampusEnrollHighP95Latency
        expr: |
          histogram_quantile(
            0.95,
            sum by (application, le) (
              rate(http_server_requests_seconds_bucket[5m])
            )
          ) > 1.5
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High p95 latency"
          description: "A CampusEnroll service exceeded the proposed p95 latency objective of 1.5 seconds."
```

## Example 4: backup too old

```yaml
groups:
  - name: campusenroll-backups
    rules:
      - alert: CampusEnrollBackupTooOld
        expr: time() - campusenroll_backup_last_success_unixtime > 86400
        for: 10m
        labels:
          severity: warning
        annotations:
          summary: "CampusEnroll backup is too old"
          description: "The last successful PostgreSQL backup is older than 24 hours."
```

Important note:

- the backup-age rule is illustrative only
- `campusenroll_backup_last_success_unixtime` is not currently exported by the repo
- a later segment would need to publish that metric intentionally

## Why these are proposals and not active claims

S27 does not add:

- Alertmanager
- active rule files mounted into Prometheus
- Grafana dashboard packs
- backup-age metric export

So the correct statement is:

`CampusEnroll HA already implements metrics and scraping. S27 adds a proposal for SLOs and alerting, but those controls are not yet claimed as active production monitoring.`

## Suggested presentation wording

Use wording like this:

`The project already exposes real Prometheus metrics and scrape targets. What S27 adds is a formal academic proposal for SLOs and example alert rules. Grafana is available as infrastructure, but we are not claiming a production-complete dashboard and alerting stack yet.`

## Useful verification commands

Verify current service metrics:

```powershell
curl.exe http://localhost:8081/actuator/prometheus
curl.exe http://localhost:8082/actuator/prometheus
curl.exe http://localhost:8083/actuator/prometheus
curl.exe http://localhost:8084/actuator/prometheus
curl.exe http://localhost:8085/actuator/prometheus
```

Verify current Prometheus targets:

```powershell
Start-Process "http://localhost:9090/targets"
```

Inspect the scrape configuration:

```powershell
Get-Content .\infra\prometheus\prometheus.yml
```

## Bottom line

CampusEnroll HA already has the metrics layer required to discuss observability credibly.

S27 closes the remaining course-language gap by adding:

- clear SLI, SLO, SLA, and alerting definitions
- realistic academic objectives
- illustrative Prometheus rule examples
- accurate boundaries about what is implemented now versus what remains future work
