# billing-service

Initial Spring Boot scaffold for the CampusEnroll HA `billing-service`.

## Segment Scope

This segment includes only:
- application bootstrap
- package structure
- basic configuration placeholder
- one health endpoint at `GET /health`

This segment does not include:
- billing entities
- invoice lifecycle logic
- payment processing
- payment status workflows
- compensation logic
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
- Port: `8084`
- Database URL: `jdbc:postgresql://localhost:5432/campusenroll`
- Database user: `campus`
- Database password: `campus_password`

Override with:
- `SERVER_PORT`
- `BILLING_SERVICE_DATASOURCE_URL`
- `BILLING_SERVICE_DATASOURCE_USERNAME`
- `BILLING_SERVICE_DATASOURCE_PASSWORD`

## Test

```bash
mvn test
```

## Next Segments

- add billing domain model
- add persistence and migrations
- define service contracts and events
