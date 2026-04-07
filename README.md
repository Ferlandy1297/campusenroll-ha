CampusEnroll HA Monorepo (Segment S00)

Overview
- Distributed microservices foundation for CampusEnroll HA.
- This segment (S00) sets up documentation, structure, and workflows only.
- No application code, Docker Compose, or service implementations yet.

Locked Scope (per Proposal 5)
- Services: API Gateway, student-service, course-service, enrollment-service, billing-service, notification.
- Infra: PostgreSQL, Redis, RabbitMQ, Prometheus, Grafana.
- Tooling: Docker Compose (next segment), GitHub Actions, Postman, k6 (load), chaos testing readiness.

Repository Structure
- docs/ — context, plans, boundaries, team workflow, and checklists.
- backend/ — service folders with README-only placeholders.
- infra/ — observability and testing scaffolds (README-only).
- postman/ — API collection scaffolding (README-only).
- db/ — database ownership and migration plan (README-only).

Status
- S00 delivers project skeleton, decisions, and conventions.
- All implementation is intentionally deferred with TODO markers.

Contributing (S00)
- Follow docs/TEAM_WORKFLOW.md and docs/CODEX_CONTEXT.md.
- Use English commit messages. Prefer Conventional Commits.

Next Steps (planned for future segments)
- Add Docker Compose, baseline configs, and placeholder health endpoints.
- Implement service contracts, message schemas, and observability wiring.
