Prometheus (S00)

Purpose
- Metrics collection and scraping for all services and components.

Status (S00)
- No configuration files yet; this folder documents expectations and wiring plans.

Conventions (planned)
- Each service exposes /metrics (Prometheus format).
- Centralized scrape config in future Docker Compose or Helm (deferred).
- Alerting rules and recording rules added post‑MVP.

TODO
- Provide base prometheus.yml and targets in a future segment.
