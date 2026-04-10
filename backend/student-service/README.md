# student-service

Spring Boot service for the CampusEnroll HA `student-service`.

## Segment Scope

This segment includes only:
- student profile CRUD for a small core model
- validation and simple in-service error handling
- JPA repository and service layer
- health endpoint at `GET /health`

This segment does not include:
- authentication or authorization
- messaging or enrollment logic
- migrations
- Docker Compose or monorepo-wide changes

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
- Port: `8081`
- Database URL: `jdbc:postgresql://localhost:5432/campusenroll`
- Database user: `campus`

Override with:
- `SERVER_PORT`
- `STUDENT_SERVICE_DATASOURCE_URL`
- `STUDENT_SERVICE_DATASOURCE_USERNAME`
- `STUDENT_SERVICE_DATASOURCE_PASSWORD`

## Endpoints

- `GET /health`
- `GET /api/students`
- `GET /api/students/{id}`
- `POST /api/students`
- `PATCH /api/students/{id}/status`

## Student Model

Fields kept intentionally small for this segment:
- `id`
- `studentCode`
- `firstName`
- `lastName`
- `email`
- `active`

Validation rules:
- `studentCode` is required and must be unique
- `firstName` is required
- `lastName` is required
- `active` must be explicit on create and status updates
- `email` is optional, but must be valid when present

Example create request:

```json
{
  "studentCode": "STU-001",
  "firstName": "Ana",
  "lastName": "Lopez",
  "email": "ana@example.com",
  "active": true
}
```

## Test

```bash
mvn test
```

## Next Segments

- add migrations for the `students` table
- define service events when cross-service integration is in scope
