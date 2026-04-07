student-service (S00)

Scope
- Owns student profiles and related attributes.
- Publishes student lifecycle events (schemas TBD).

Out of Scope (S00)
- No implementation, endpoints, or DB schema yet.
- No auth, business rules, or Compose files.

Data Ownership
- PostgreSQL schema: students.* (TBD)

Integration
- RabbitMQ for events (TBD exchanges/queues).
- Redis optional for caching (TBD keys/TTL).

TODO (next segments)
- Choose stack and framework.
- Define REST surface and event contracts.
- Migrations and seed strategy.
