course-service (S00)

Scope
- Owns course catalog: courses, sections, schedules, capacity metadata.
- Publishes course lifecycle events (schemas TBD).

Out of Scope (S00)
- No implementation, endpoints, or DB schema yet.
- No auth, business rules, or Compose files.

Data Ownership
- PostgreSQL schema: courses.* (TBD)

Integration
- RabbitMQ for events (TBD exchanges/queues).
- Redis optional for caching (TBD usage).

TODO (next segments)
- Choose stack and framework.
- Define REST surface and event contracts.
- Migrations and seed strategy.
