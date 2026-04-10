# course-service

Spring Boot service for the CampusEnroll HA academic catalog slice owned by `course-service`.

## Segment Scope

This segment includes only:
- course catalog entities for `Course`, `AcademicPeriod`, `Section`, and embedded `ScheduleBlock`
- JPA repositories and service-layer orchestration
- DTOs, request validation, and basic API error handling
- catalog endpoints plus the existing health endpoint at `GET /health`

This segment does not include:
- program or career entities
- prerequisites
- authentication or authorization
- messaging or enrollment logic
- Docker Compose or monorepo-wide changes

## Domain Model

- `Course`
  - `id`
  - `courseCode` (required, unique)
  - `name` (required)
  - `credits` (required, zero or greater)
  - `active` (required)
- `AcademicPeriod`
  - `id`
  - `name` (required)
  - `active` (required)
- `Section`
  - `id`
  - `sectionCode` (required)
  - `capacity` (required, greater than zero)
  - `active` (required)
  - references one `Course`
  - references one `AcademicPeriod`
  - contains one or more `ScheduleBlock` entries
- `ScheduleBlock`
  - `dayOfWeek`
  - `startTime`
  - `endTime` (`endTime` must be after `startTime`)

## API

- `GET /health`
- `GET /api/courses`
- `GET /api/courses/{id}`
- `POST /api/courses`
- `GET /api/periods`
- `POST /api/periods`
- `GET /api/sections`
- `POST /api/sections`

Example create course request:

```json
{
  "courseCode": "CS101",
  "name": "Introduction to Programming",
  "credits": 4,
  "active": true
}
```

Example create section request:

```json
{
  "sectionCode": "CS101-A",
  "capacity": 30,
  "active": true,
  "courseId": 1,
  "academicPeriodId": 1,
  "scheduleBlocks": [
    {
      "dayOfWeek": "MONDAY",
      "startTime": "08:00:00",
      "endTime": "09:30:00"
    }
  ]
}
```

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

For this segment, Hibernate is configured with `ddl-auto: update` so the catalog tables can be created locally without migrations yet.

Override with:
- `SERVER_PORT`
- `COURSE_SERVICE_DATASOURCE_URL`
- `COURSE_SERVICE_DATASOURCE_USERNAME`
- `COURSE_SERVICE_DATASOURCE_PASSWORD`

## Test

```bash
mvn test
```

## Notes

- `courseCode` uniqueness is enforced in the service layer and by the database constraint.
- Error responses are intentionally small: status, error, message, and optional field errors.
- No cross-service integration, enrollment behavior, or event publishing is included in this segment.
