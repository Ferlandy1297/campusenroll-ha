Service Boundaries — Segment S00

Guiding Principles
- Each service owns its data (PostgreSQL schema/db) and publishes domain events.
- API Gateway fronts all client traffic; services communicate via HTTP and events.
- Redis used for caching and transient coordination where appropriate.
- RabbitMQ used for async command/event patterns.

API Gateway
- Responsibilities: request routing, aggregation where needed, rate limiting, request validation.
- Non‑Responsibilities (S00): authentication/authorization, business logic.
- Dependencies: all downstream services; observability hooks.

student-service
- Responsibilities: CRUD for student profiles and core attributes.
- Non‑Responsibilities: enrollment rules, billing, course catalog ownership.
- Data Ownership: students.* schema in PostgreSQL (TBD).
- Events: StudentCreated/Updated (TBD schemas).

course-service
- Responsibilities: CRUD for courses, sections, schedules, capacity metadata.
- Non‑Responsibilities: enrollment assignment, billing, notifications.
- Data Ownership: courses.* schema in PostgreSQL (TBD).
- Events: CourseCreated/Updated (TBD schemas).

enrollment-service
- Responsibilities: track student enrollments to course sections; statuses.
- Non‑Responsibilities: payment processing, student identity details, course definitions.
- Data Ownership: enrollments.* schema in PostgreSQL (TBD).
- Events: EnrollmentCreated/Updated (TBD schemas).

billing-service
- Responsibilities: invoice lifecycle, charges, and payment status tracking.
- Non‑Responsibilities: enrollment rules, course definitions, notifications content.
- Data Ownership: billing.* schema in PostgreSQL (TBD).
- Events: InvoiceCreated/Paid/Failed (TBD schemas).

notification
- Responsibilities: send notifications triggered by domain events; channel fan‑out.
- Non‑Responsibilities: business rules that decide outcomes.
- Data Ownership: minimal persistence (TBD) for outbox/retry if needed.
- Events: Consumes domain events; may emit NotificationSent (TBD schema).

Cross‑Cutting Concerns (planned)
- Observability: Prometheus metrics; Grafana dashboards; structured logs.
- Reliability: idempotency keys where needed; DLQs; retries.
- Testing: API via Postman; load via k6; chaos via TBD tooling.

Open TODOs
- Define API surfaces and message/event schemas per service.
- Align on error models, pagination, sorting, and filtering conventions.
- Choose service implementation tech stacks.
