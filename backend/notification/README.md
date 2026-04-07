notification (S00)

Scope
- Sends notifications in response to domain events; supports multiple channels (TBD).

Out of Scope (S00)
- No implementation, endpoints, or DB schema yet.
- No auth, content templates, or Compose files.

Data Ownership
- Minimal persistence (TBD) if outbox/retry patterns are adopted.

Integration
- Consumes events from RabbitMQ; may emit NotificationSent (TBD schema).
- Redis optional for rate limiting or dedupe (TBD).

TODO (next segments)
- Choose stack and framework.
- Define event contracts and delivery policies.
- Observability metrics and dashboards.
