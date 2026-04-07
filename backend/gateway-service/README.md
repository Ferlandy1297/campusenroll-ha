gateway-service (API Gateway, S00)

Scope
- Fronts all client traffic; routes requests to internal services.
- Applies request validation and rate limiting (policies TBD).

Out of Scope (S00)
- No implementation or configuration yet.
- No authentication or authorization logic in this segment.

Integration
- Upstream: clients.
- Downstream: student, course, enrollment, billing, notification.

Observability
- Expose basic health/metrics in future segments.

TODO (next segments)
- Choose gateway tech (TBD: e.g., Nginx, Kong, Envoy, or code‑based gateway).
- Define routing, validation, and error model conventions.
