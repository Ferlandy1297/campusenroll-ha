# student-service

Initial Spring Boot scaffold for the CampusEnroll HA `student-service`.

## Segment Scope

This segment includes only:
- application bootstrap
- package structure
- basic configuration placeholder
- one health endpoint at `GET /health`

This segment does not include:
- student entities or CRUD
- authentication or authorization
- messaging or enrollment logic
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

## Test

```bash
mvn test
```

## Next Segments

- add student domain model
- add persistence and migrations
- define service contracts and events
