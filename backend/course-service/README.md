# course-service

Initial Spring Boot scaffold for the CampusEnroll HA `course-service`.

## Segment Scope

This segment includes only:
- application bootstrap
- package structure
- basic configuration placeholder
- one health endpoint at `GET /health`

This segment does not include:
- course entities or CRUD
- program or career entities
- sections, schedules, or prerequisites
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
- Port: `8082`
- Database URL: `jdbc:postgresql://localhost:5432/campusenroll`
- Database user: `campus`
- Database password: `campus_password`

Override with:
- `SERVER_PORT`
- `COURSE_SERVICE_DATASOURCE_URL`
- `COURSE_SERVICE_DATASOURCE_USERNAME`
- `COURSE_SERVICE_DATASOURCE_PASSWORD`

## Test

```bash
mvn test
```

## Next Segments

- add course domain model
- add persistence and migrations
- define service contracts and events
