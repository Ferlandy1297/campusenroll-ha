Plan de Trabajo — Segment S00

Objective
- Deliver a clean, reviewable monorepo foundation aligned with Proposal 5.
- No code or Compose in this segment; documentation and structure only.

Scope
- Services: API Gateway, student, course, enrollment, billing, notification.
- Infra: PostgreSQL, Redis, RabbitMQ, Prometheus, Grafana.
- Tooling: GitHub Actions (planning), Postman (planning), k6 and chaos readiness.

Phases (S00)
1) Establish repository structure and conventions.
2) Capture service boundaries and ownership.
3) Define team workflow and delivery checklist.
4) Document observability and testing readiness.

Deliverables
- README.md, .gitignore.
- docs/: CODEX_CONTEXT, PLAN_TRABAJO, TEAM_WORKFLOW, SERVICE_BOUNDARIES, DELIVERABLES_CHECKLIST.
- backend/: one README per service.
- infra/: READMEs for prometheus, grafana, k6, chaos.
- postman/README.md and db/README.md.

Out of Scope (Defer)
- Implementation details, API contracts, event schemas, DB migrations.
- Docker Compose and runtime configuration.

Risks / Notes
- Avoid premature tech lock‑in; keep docs implementation‑agnostic.
- Ensure future segments can extend without restructuring.

Next Segments (Preview)
- S01: Compose, base configs, health endpoints, CI scaffolding.
- S02: Contracts (REST + events), baseline schemas, initial pipelines.
- S03+: Incremental service implementation and observability wiring.
