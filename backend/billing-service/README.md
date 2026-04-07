billing-service (S00)

Scope
- Owns invoices, charges, and payment status tracking.
- Publishes billing lifecycle events (schemas TBD).

Out of Scope (S00)
- No implementation, endpoints, or DB schema yet.
- No auth, business rules, or Compose files.

Data Ownership
- PostgreSQL schema: billing.* (TBD)

Integration
- RabbitMQ for events (TBD exchanges/queues).
- Redis optional for caching/idempotency data.

TODO (next segments)
- Choose stack and framework.
- Define REST surface and event contracts.
- Migrations and seed strategy.
