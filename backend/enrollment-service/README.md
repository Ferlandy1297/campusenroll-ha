enrollment-service (S00)

Scope
- Owns student-to-course enrollment records and statuses.
- Publishes enrollment lifecycle events (schemas TBD).

Out of Scope (S00)
- No implementation, endpoints, or DB schema yet.
- No auth, business rules, or Compose files.

Data Ownership
- PostgreSQL schema: enrollments.* (TBD)

Integration
- RabbitMQ for events (TBD exchanges/queues).
- Redis optional for transient coordination.

TODO (next segments)
- Choose stack and framework.
- Define REST surface and event contracts.
- Migrations and seed strategy.
