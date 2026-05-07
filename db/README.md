# Database Deliverable

This folder contains the database-focused deliverables for the implemented CampusEnroll HA service domains. For Base de Datos II, `db/schema.sql` is the authoritative relational design to review, even though some backend services still rely on local Hibernate behavior during development.

## Files

- `db/schema.sql`
  - Creates the normalized PostgreSQL schema for students, courses, academic periods, sections, schedule blocks, enrollments, and billings.
  - Encodes the key relational constraints, check constraints, indexes, and partial unique indexes required by this segment.
- `db/data.sql`
  - Loads a small, coherent demo dataset for manual testing.
  - Uses `INSERT ... ON CONFLICT` so the seed can be re-applied on the same local schema with minimal friction.

## Execution Order

1. Run `db/schema.sql`
2. Run `db/data.sql`

`db/schema.sql` recreates the tracked domain tables so the delivered structure is exact and reviewable. Run it on the local development database, not on a database you need to preserve as-is.

## PowerShell and Docker Examples

Start only PostgreSQL if needed:

```powershell
docker compose up -d postgres
docker compose ps postgres
```

Run the schema from PowerShell against the local container:

```powershell
Get-Content -Raw .\db\schema.sql | docker exec -i campusenroll-postgres psql -U campus -d campusenroll -v ON_ERROR_STOP=1
```

Load demo data:

```powershell
Get-Content -Raw .\db\data.sql | docker exec -i campusenroll-postgres psql -U campus -d campusenroll -v ON_ERROR_STOP=1
```

Quick verification:

```powershell
docker exec -i campusenroll-postgres psql -U campus -d campusenroll -c "\dt"
docker exec -i campusenroll-postgres psql -U campus -d campusenroll -c "SELECT id, student_code, first_name, last_name FROM students ORDER BY id;"
docker exec -i campusenroll-postgres psql -U campus -d campusenroll -c "SELECT id, section_code, academic_period_id, course_id FROM sections ORDER BY id;"
```

Defaults already documented in this repo:

- container: `campusenroll-postgres`
- database: `campusenroll`
- user: `campus`
- password: `campus_password`
- host port: `5432`

## DBeaver

1. Create a PostgreSQL connection with:
   - Host: `localhost`
   - Port: `5432`
   - Database: `campusenroll`
   - Username: `campus`
   - Password: `campus_password`
2. Open a SQL Editor on that connection.
3. Execute `db/schema.sql`.
4. Execute `db/data.sql`.
5. Refresh the navigator and inspect the tables under the default schema.

## Assumptions and Limitations

- This deliverable consolidates the already implemented service domains into one normalized PostgreSQL model for course review.
- `notification` remains scaffold-only, so no notification tables are created yet.
- The authoritative SQL model uses a normalized `schedule_blocks` table and PostgreSQL partial indexes instead of service-local persistence workarounds such as `section_schedule_blocks` or helper uniqueness columns.
- The schema intentionally models the business rules directly with relational constraints. In particular:
  - duplicate active enrollment is enforced with a PostgreSQL partial unique index
  - duplicate pending billing is enforced with a PostgreSQL partial unique index
- Some rules are still service-level and are not enforced with triggers in this segment:
  - every section should include at least one schedule block
  - seat-capacity enforcement against current enrollment count
  - schedule-overlap validation across sections
- Backend services in this repo currently mix `ddl-auto: update` and `ddl-auto: none`, and some local persistence details can differ from this SQL deliverable. For Base de Datos II, `db/schema.sql` is the authoritative schema to review.
- No migrations are included yet in this segment.
