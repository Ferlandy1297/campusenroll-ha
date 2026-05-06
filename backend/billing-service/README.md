# billing-service

Spring Boot billing-service for the CampusEnroll HA billing domain segment.

## Segment Scope

This segment includes:
- billing entity, repository, service, DTOs, and controller endpoints
- local validation and simple error handling for billing requests
- one health endpoint at `GET /health`
- focused unit tests for controller and service behavior

This segment does not include:
- cross-service HTTP calls
- payment gateway integration
- messaging, sagas, or compensation workflows
- authentication or authorization
- Docker Compose changes
- database migrations

## Stack

- Java 17
- Spring Boot
- Maven
- Spring Web
- Spring Boot Actuator
- Spring Validation
- Spring Data JPA
- PostgreSQL driver

## Run Locally

```bash
mvn spring-boot:run
```

Default placeholders:
- Port: `8084`
- Database URL: `jdbc:postgresql://localhost:5432/campusenroll`
- Database user: `campus`
- Database password: `campus_password`

Override with:
- `SERVER_PORT`
- `BILLING_SERVICE_DATASOURCE_URL`
- `BILLING_SERVICE_DATASOURCE_USERNAME`
- `BILLING_SERVICE_DATASOURCE_PASSWORD`

## Billing API

Endpoints:
- `GET /api/billings`
- `GET /api/billings/{id}`
- `POST /api/billings`
- `PATCH /api/billings/{id}/status`

Billing fields in this segment:
- `id`
- `enrollmentId`
- `amount`
- `currency`
- `status`
- `createdAt`

Supported statuses:
- `PENDING`
- `PAID`
- `CANCELLED`

Validation rules:
- `enrollmentId` is required
- `amount` is required and must be greater than zero
- `currency` is required
- `status` is required and must be valid
- only one active `PENDING` billing is allowed per `enrollmentId`

Example create payload:

```json
{
  "enrollmentId": 100,
  "amount": 150.75,
  "currency": "USD",
  "status": "PENDING"
}
```

Simple error response shape:

```json
{
  "timestamp": "2026-05-06T00:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "details": [
    "amount must be greater than zero"
  ]
}
```

## Persistence Note

This segment uses a JPA entity and repository only. Because migrations are intentionally out of scope here and `spring.jpa.hibernate.ddl-auto` remains `none`, the `billings` table must be provisioned externally before running the service against PostgreSQL.

## Test

```bash
mvn test
```

## Next Segments

- add migrations for the `billings` table
- define service contracts and domain events
- integrate billing with enrollment and payment workflows later
