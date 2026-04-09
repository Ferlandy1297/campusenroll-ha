# notification

Initial Spring Boot scaffold for the CampusEnroll HA `notification` service.

## Segment Scope

This segment includes only:
- application bootstrap
- package structure
- basic configuration placeholder
- one health endpoint at `GET /health`

This segment does not include:
- notification entities
- channel logic
- email, SMS, or push sending logic
- event consumers
- retry logic
- authentication or authorization
- messaging implementation
- Docker Compose changes

## Stack

- Java 17
- Spring Boot
- Maven
- Spring Web
- Spring Boot Actuator
- Spring Validation

## Run Locally

```bash
mvn spring-boot:run
```

Default placeholders:
- Port: `8085`

Override with:
- `SERVER_PORT`

## Test

```bash
mvn test
```

## Next Segments

- add notification domain model
- define event contracts and delivery policies
- add channel adapters and retry handling
