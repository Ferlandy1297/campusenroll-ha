# Prometheus

## Purpose

`infra/prometheus/` contains the local Prometheus configuration used by CampusEnroll HA.

It now covers:

- scrape configuration for Prometheus itself
- scrape targets for the five Spring Boot services through `/actuator/prometheus`
- active alert rules for service availability, missing targets, HTTP 5xx rate, and p95 latency

## Current files

- `prometheus.yml`
  - base scrape configuration and `rule_files` loading
- `rules/campusenroll-alerts.yml`
  - active Prometheus alert rules for the current local stack

## What is implemented now

- Prometheus runs as a real container in `docker-compose.yml`
- the five application services are scraped in HA-readiness mode
- the rules file is mounted into the Prometheus container
- alerts are visible in the Prometheus UI at:
  - `http://localhost:9090/alerts`
  - `http://localhost:9090/rules`

## What is not implemented

- Alertmanager routing
- email, Slack, Teams, or pager notifications
- production-grade dashboard and on-call workflow

## Important notes

- after changing `prometheus.yml` or the rules under `infra/prometheus/rules/`, restart Prometheus because the config is bind-mounted
- the p95 latency alert depends on `http.server.requests` histogram buckets, which are now enabled through the Spring Boot service `application.yml` files
- Grafana remains available infrastructure, but the repo does not claim a production-complete dashboard package

## Useful commands

```powershell
docker compose -f docker-compose.yml -f docker-compose.apps.yml -f docker-compose.ha-demo.yml restart prometheus
curl.exe http://localhost:9090/-/ready
Start-Process "http://localhost:9090/alerts"
Start-Process "http://localhost:9090/rules"
docker compose -f docker-compose.yml -f docker-compose.apps.yml -f docker-compose.ha-demo.yml logs prometheus --tail=120
```
