# CampusEnroll HA Postman Assets

## Collection Purpose

`campusenroll-ha.postman_collection.json` contains direct local API checks for the implemented CampusEnroll HA microservices. The collection is organized by domain area and is intended to validate the current HTTP surface of the system.

## Environment Purpose

`campusenroll-ha.local.postman_environment.json` stores:

- local base URLs for each service
- reusable resource IDs captured from successful creation requests

## Files

- `postman/campusenroll-ha.postman_collection.json`
- `postman/campusenroll-ha.local.postman_environment.json`

## How To Import Into Postman

1. Import `postman/campusenroll-ha.postman_collection.json`.
2. Import `postman/campusenroll-ha.local.postman_environment.json`.
3. Select the `CampusEnroll HA Local` environment before running requests.

## Recommended Execution Order

1. Run `00 - Health Checks`.
2. Run `01 - Students`, starting with `Create Student` before ID-based student requests.
3. Run `02 - Academic Catalog` in this order: `Create Course`, `Create Academic Period`, then `Create Section`.
4. Run `03 - Enrollments` after `student_id` and `section_id` are populated.
5. Run `04 - Billings` after `enrollment_id` is populated.
6. Run `05 - Notification` for the current notification health check.

## Service Port Mapping

| Service | Environment Variable | Local Default |
| --- | --- | --- |
| student-service | `student_service_url` | `http://localhost:8081` |
| course-service | `course_service_url` | `http://localhost:8082` |
| enrollment-service | `enrollment_service_url` | `http://localhost:8083` |
| billing-service | `billing_service_url` | `http://localhost:8084` |
| notification | `notification_service_url` | `http://localhost:8085` |

## What Postman Validates Today

- service health endpoints
- students CRUD-lite flow
- academic catalog flow
- enrollment creation and status update
- billing creation and status update
- current notification health endpoint

## What Postman Does Not Prove By Itself

The collection triggers the business actions, but the following evidence must still be verified outside Postman:

- Redis cache keys after repeated `GET /api/courses`
- RabbitMQ exchange, queue, and binding state
- publication logs in `enrollment-service` and `billing-service`
- consumer logs in `notification`
- k6 summaries
- Prometheus and Grafana access

Recommended pairings:

1. After running `GET /api/courses` twice, inspect Redis with:
   - `docker exec -i campusenroll-redis redis-cli --scan --pattern "courses::*"`
2. After `POST /api/enrollments` and `PATCH /api/billings/{id}/status`, inspect RabbitMQ and logs.

## Notes

- All services must be running locally before testing the collection.
- Authentication is not included yet, so the collection does not define auth headers or tokens.
- Gateway routes are not included because gateway integration is not the active validation path.
- `POST /api/enrollments` sends only `studentId` and `sectionId`; the service sets the initial enrollment status.
