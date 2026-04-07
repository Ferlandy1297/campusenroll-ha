Team Workflow — Segment S00

Branching Strategy
- Default branch: main
- Feature branches: feature/<short-scope>
- Documentation branches: docs/<topic>
- Release tags: vMAJOR.MINOR.PATCH (future segments)

Commit Convention
- English; prefer Conventional Commits.
- Examples:
  - chore(docs): add service boundaries
  - docs(infra): outline Grafana expectations
  - ci: prepare GitHub Actions draft (no code)

Pull Requests
- Small, focused PRs tied to a single segment or task.
- Include context link to docs/CODEX_CONTEXT.md and relevant docs.
- Request at least one review; use checklists from docs/DELIVERABLES_CHECKLIST.md.

Reviews
- Scope check: matches segment and non‑goals.
- Clarity check: docs are concise and actionable.
- Future‑proofing: structure supports next segments without churn.

Issue Labels (suggested)
- segment:S00, area:docs, area:infra, area:backend, type:planning, type:readiness

Decision Records
- Capture important decisions inline in relevant README docs with a short “Decision” section.
- Use TODO markers for deferred decisions.

CI/CD (S00)
- No pipelines yet; document intended checks in docs and wire up in a future segment.
