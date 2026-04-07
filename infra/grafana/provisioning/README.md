Grafana Provisioning (S01)

Place provisioning files here if you want Grafana to auto-load configuration:
- `datasources/` for Prometheus and other data sources (YAML)
- `dashboards/` for dashboard definitions (JSON) and `dashboards.yml` to load them

Compose mounts this directory at `/etc/grafana/provisioning` inside the Grafana container.

Example (not provided in S01):
- `datasources/prometheus.yml` pointing to `http://prometheus:9090`
- `dashboards/` with baseline dashboards for services

Note: This segment intentionally avoids shipping default dashboards or datasources. Use the UI or add files here as needed locally.

