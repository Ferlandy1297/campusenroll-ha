# enrollment-service

Spring Boot enrollment domain segment for the CampusEnroll HA `enrollment-service`.

## Segment Scope

This segment includes:
- enrollment entity and status model
- JPA repository
- service-layer enrollment rules
- DTOs and REST endpoints
- RabbitMQ event publication for successful enrollment creation
- request validation and basic error handling
- focused unit and web tests
- health endpoint at `GET /health`

This segment does not include:
- cross-service validation for student or section existence
- seat-capacity validation
- schedule-overlap validation
- billing compensation
- authentication or authorization
- sagas, retries, DLQs, or Docker Compose changes

## Stack

- Java 17
- Spring Boot
- Maven
- Spring Web
- Spring Boot Actuator
- Spring Validation
- Spring Data JPA
- PostgreSQL driver
- Spring AMQP

## Run Locally

```bash
mvn spring-boot:run
```

Default placeholders:
- Port: `8083`
- Database URL: `jdbc:postgresql://localhost:5432/campusenroll`
- Database user: `campus`

Override with:
- `SERVER_PORT`
- `ENROLLMENT_SERVICE_DATASOURCE_URL`
- `ENROLLMENT_SERVICE_DATASOURCE_USERNAME`
- `ENROLLMENT_SERVICE_DATASOURCE_PASSWORD`
- `RABBITMQ_HOST`
- `RABBITMQ_PORT`
- `RABBITMQ_USERNAME`
- `RABBITMQ_PASSWORD`
- `APP_EVENTS_EXCHANGE`
- `APP_ENROLLMENT_CREATED_ROUTING_KEY`

Current JPA behavior:
- `spring.jpa.hibernate.ddl-auto=update`

This keeps the segment runnable locally without introducing migrations yet.

## API

- `GET /health`
- `GET /api/enrollments`
- `GET /api/enrollments/{id}`
- `POST /api/enrollments`
- `PATCH /api/enrollments/{id}/status`

### Enrollment fields

- `id`
- `studentId`
- `sectionId`
- `status`
- `enrolledAt`

### Status values

- `ENROLLED`
- `CANCELLED`

### Validation rules in this segment

- `studentId` is required
- `sectionId` is required
- `status` must be a valid enum value on status updates
- duplicate active enrollment is rejected for the same `studentId + sectionId`

### Example create request

```json
{
  "studentId": 100,
  "sectionId": 200
}
```

### Example status update request

```json
{
  "status": "CANCELLED"
}
```

## RabbitMQ Event Flow

On successful `POST /api/enrollments`, the service publishes `EnrollmentCreatedEvent` to:

- Exchange: `campusenroll.events`
- Routing key: `enrollment.created`

Event fields:

- `eventId`
- `enrollmentId`
- `studentId`
- `sectionId`
- `status`
- `occurredAt`

If RabbitMQ is unavailable, the REST operation still succeeds and the publish failure is logged.

## Test

```bash
mvn test
```

## Next Segments

- add schema migrations
- integrate downstream validation and workflows when boundaries allow
- harden event delivery with outbox/retry patterns if the project scope expands
