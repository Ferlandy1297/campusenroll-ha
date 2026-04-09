# enrollment-service

Initial Spring Boot scaffold for the CampusEnroll HA `enrollment-service`.

## Segment Scope

This segment includes only:
- application bootstrap
- package structure
- basic configuration placeholder
- one health endpoint at `GET /health`

This segment does not include:
- enrollment entities or statuses beyond the service scaffold
- enrollment validation rules
- seat, duplicate-enrollment, or schedule-overlap checks
- billing compensation
- authentication or authorization
- messaging or Docker Compose changes

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
- Port: `8083`
- Database URL: `jdbc:postgresql://localhost:5432/campusenroll`
- Database user: `campus`

Override with:
- `SERVER_PORT`
- `ENROLLMENT_SERVICE_DATASOURCE_URL`
- `ENROLLMENT_SERVICE_DATASOURCE_USERNAME`
- `ENROLLMENT_SERVICE_DATASOURCE_PASSWORD`

## Test

```bash
mvn test
```

## Next Segments

- add enrollment domain model
- add persistence and migrations
- define service contracts and events
