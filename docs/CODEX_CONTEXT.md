CampusEnroll HA — Codex Context (Segment S00)

Purpose
- Capture the authoritative context for this monorepo foundation.
- Aligns with Proposal 5 and the locked scope provided by the owner.

Segment S00 Boundaries
- Create structure and documentation only.
- Do not add application code, Docker Compose, or service implementations yet.
- Prepare for: API Gateway, 5 domain services, PostgreSQL, Redis, RabbitMQ, Prometheus, Grafana, GitHub Actions, Postman, k6, and chaos testing readiness.

Assumptions (Non‑Business)
- Stateless services behind an API Gateway; service‑owned data stores (PostgreSQL).
- Event-driven integration via RabbitMQ; Redis for caching and ephemeral needs.
- Observability via Prometheus metrics and Grafana dashboards.
- CI: GitHub Actions; API testing via Postman; load via k6; chaos via a TBD tool.
- No authentication or frontend in S00. No business rules defined here.

Non‑Goals (S00)
- No endpoint definitions, message schemas, or DB schemas.
- No Docker files, Compose, or Helm charts.
- No language or framework lock‑in at this time.

Repository Conventions
- English commit messages; prefer Conventional Commits (e.g., chore(docs): add service boundaries).
- Use TODO notes for deferred decisions; avoid inventing business rules.
- Keep documentation concise, professional, and implementation‑agnostic.

Monorepo Layout (S00)
- docs/: Planning, team workflow, service boundaries, and checklists.
- backend/: One folder per service with scoped README.
- infra/: Prometheus, Grafana, k6, chaos folders with READMEs only.
- postman/: Collection conventions and environments (no files yet).
- db/: Ownership model and migration strategy notes (no SQL yet).

Readiness Checklist (High‑Level)
- Structure: Created for all services and infra.
- Documentation: Baseline READMEs and process docs present.
- Observability: Placeholders and conventions captured.
- Testing: Load and chaos readiness documented.
- Delivery: Single clean commit when reviewed.

Open TODOs
- Select implementation stacks per service (language, frameworks).
- Define service contracts: REST surfaces and event schemas.
- Add Docker Compose and seed observability configs in a future segment.
- Establish CI pipelines in GitHub Actions per service and infra.
