Local Infrastructure (S01)

This brings up local infra for CampusEnroll HA using Docker Compose, aligned with Proposal 5. It includes only: Postgres, Redis, RabbitMQ, Prometheus, and Grafana. No gateway, services, auth, or frontend are included in S01.

Prerequisites
- Docker Desktop (Windows) with WSL2 or Hyper-V
- Windows PowerShell

Setup (PowerShell)
- cd to the repo root: `cd campusenroll-ha`
- Copy env file: `Copy-Item .env.example .env`
- Optionally edit `.env` to change ports and credentials

Start
- Bring up infra: `docker compose up -d`
- Check status: `docker compose ps`
- Tail logs: `docker compose logs -f`

Stop and Clean
- Stop: `docker compose down`
- Stop and remove volumes: `docker compose down -v` (destroys data)

Services and Ports (from .env defaults)
- Postgres: `localhost:5432` (db: `campusenroll`, user: `campus`)
- Redis: `localhost:6379`
- RabbitMQ AMQP: `localhost:5672`
- RabbitMQ UI: `http://localhost:15672` (user/pass from `.env`)
- Prometheus UI: `http://localhost:9090`
- Grafana UI: `http://localhost:3000` (admin user/pass from `.env`)

RabbitMQ Management UI
- URL: `http://localhost:15672`
- Login: `RABBITMQ_DEFAULT_USER` / `RABBITMQ_DEFAULT_PASS` from `.env`

Grafana UI
- URL: `http://localhost:3000`
- Login: `GRAFANA_ADMIN_USER` / `GRAFANA_ADMIN_PASSWORD` from `.env`
- To add Prometheus datasource via UI:
  - Navigate to Connections > Data sources > Add data source > Prometheus
  - Set URL to `http://prometheus:9090` and Save & test
- Alternatively, place provisioning files under `infra/grafana/provisioning/`

Prometheus
- Config file is mounted from `infra/prometheus/prometheus.yml`
- Default scrape: Prometheus itself (for now)

Health Checks
- Postgres: `pg_isready`
- Redis: `redis-cli ping`
- RabbitMQ: `rabbitmq-diagnostics -q ping`
- Prometheus and Grafana run without explicit healthchecks here to avoid tooling assumptions inside images.

Troubleshooting
- Show containers: `docker ps`
- Inspect logs for a service: `docker compose logs <service>` (e.g., `postgres`)
- If ports are already in use, change them in `.env` and rerun `docker compose up -d`

