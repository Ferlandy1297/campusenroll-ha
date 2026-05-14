# notification

Spring Boot notification service for the CampusEnroll HA RabbitMQ evidence flow.

## Segment Scope

This segment includes only:
- application bootstrap
- package structure
- RabbitMQ queue/exchange binding for business event evidence
- event listener for enrollment and billing events
- in-memory evidence recording plus logs
- one health endpoint at `GET /health`

This segment does not include:
- notification entities
- channel logic
- email, SMS, or push sending logic
- retry logic
- authentication or authorization
- persistence for delivered notifications
- Docker Compose changes

## Stack

- Java 17
- Spring Boot
- Maven
- Spring Web
- Spring Boot Actuator
- Spring Validation
- Spring AMQP

## Run Locally

```bash
mvn spring-boot:run
```

Default placeholders:
- Port: `8085`

Override with:
- `SERVER_PORT`
- `RABBITMQ_HOST`
- `RABBITMQ_PORT`
- `RABBITMQ_USERNAME`
- `RABBITMQ_PASSWORD`
- `APP_EVENTS_EXCHANGE`
- `APP_NOTIFICATION_QUEUE`
- `APP_ENROLLMENT_CREATED_ROUTING_KEY`
- `APP_BILLING_STATUS_CHANGED_ROUTING_KEY`

## RabbitMQ Event Flow

The service binds `notification.events` to the shared topic exchange and consumes:

- `enrollment.created`
- `billing.status.changed`

Current behavior:

- logs clear evidence when an event is received
- stores evidence strings in memory for internal verification and tests
- ignores unsupported routing keys with a warning
- keeps the implementation provider-free: no email, SMS, or push integration

## Test

```bash
mvn test
```

## Next Segments

- add notification domain model
- add delivery channels if the project later needs real notifications
- add retry and DLQ handling if reliability requirements grow
