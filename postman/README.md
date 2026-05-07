# CampusEnroll HA Postman Assets

## Collection Purpose

`campusenroll-ha.postman_collection.json` contains direct local API checks for the implemented CampusEnroll HA microservices. The collection is organized by domain area and uses simple tests for expected status codes, JSON responses, and ID capture after successful `POST` requests.

## Environment Purpose

`campusenroll-ha.local.postman_environment.json` stores:

- local base URLs for each service
- reusable resource IDs captured from successful creation requests

The environment is intended for local development only.

## Files

- `postman/campusenroll-ha.postman_collection.json`
- `postman/campusenroll-ha.local.postman_environment.json`

## How To Import Into Postman

1. Import `postman/campusenroll-ha.postman_collection.json`.
2. Import `postman/campusenroll-ha.local.postman_environment.json`.
3. Select the `CampusEnroll HA Local` environment in Postman before running requests.

## Recommended Execution Order

1. Run `00 - Health Checks` to confirm the local services are available.
2. Run `01 - Students`, starting with `Create Student` before any ID-based student request.
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

## Notes

- All services must be running locally before testing the collection.
- Authentication is not included yet, so the collection does not define auth headers or tokens.
- Messaging, event-driven flows, and distributed end-to-end scenarios are not included yet.
- Gateway routes are not included because gateway integration is not confirmed for this collection.
- `POST /api/enrollments` is aligned to the current implementation and sends `studentId` plus `sectionId`; the service sets the initial enrollment status.
