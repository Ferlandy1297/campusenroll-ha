Database (S00)

Model
- Service‑owned PostgreSQL schemas/databases (no shared write ownership).
- Redis as cache/ephemeral store where appropriate.

Status (S00)
- No DDL, migrations, or seeds defined.

Conventions (planned)
- Migrations managed per service (tooling TBD).
- Naming: snake_case tables/columns; UTC timestamps.
- Foreign relationships enforced within a service boundary only.

TODO
- Choose migration tool(s) per service stack.
- Define initial schemas aligned to service contracts.
