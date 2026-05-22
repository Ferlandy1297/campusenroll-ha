# EXPLAIN And Index Evidence

## Purpose

This document hardens the course evidence around PostgreSQL query planning for CampusEnroll HA.

It does not claim that these commands were already executed for every presentation. It provides repeatable evidence guidance that can be:

- run live during preparation or the final demo
- captured as screenshots or terminal output
- used as supporting explanation when defending index design decisions

## Why EXPLAIN matters in Database II

Database II is not only about writing correct SQL. It is also about understanding how the database plans and executes that SQL.

`EXPLAIN` helps answer questions such as:

- Is PostgreSQL scanning the whole table or using an index?
- Is the join strategy reasonable for the filter shape?
- Is the query mostly served from memory or does it need disk reads?
- Is the cost concentrated in planning, sorting, joining, or filtering?

For a course presentation, query plans are valuable because they connect three things:

- the relational design in `db/schema.sql`
- the indexes created for the project
- the real execution behavior of representative queries

## What EXPLAIN, ANALYZE, and BUFFERS mean

- `EXPLAIN`
  - shows the planned execution path without running the query
- `EXPLAIN ANALYZE`
  - executes the query and shows what actually happened, including row counts and timing
- `EXPLAIN (ANALYZE, BUFFERS)`
  - adds shared-buffer statistics so you can discuss memory hits versus physical reads

Recommended classroom rule:

- use plain `EXPLAIN` if you only want the plan shape
- use `EXPLAIN (ANALYZE, BUFFERS)` when you want defensible runtime evidence

## Important interpretation note for this repository

CampusEnroll HA ships with a very small demo dataset in `db/data.sql`.

That means PostgreSQL may legitimately choose `Seq Scan` for some of these queries because scanning three rows can be cheaper than using an index. That is not a failure and it does not prove the indexes are useless.

For this project, the point of the evidence is:

- to show that the schema has real indexes with a clear purpose
- to show how to inspect the planner honestly
- to explain why plan choice depends on data size, selectivity, and ordering requirements

## Environment assumptions

The examples below assume:

- repo root in PowerShell
- PostgreSQL running in `campusenroll-postgres`
- database loaded with `db/schema.sql` and `db/data.sql`

Base verification:

```powershell
docker compose up -d postgres
docker compose ps postgres
powershell -ExecutionPolicy Bypass -File infra/backups/verify-database.ps1
```

## Representative query A: active courses ordered by `course_code`

Purpose:

- matches a common catalog read pattern
- relates directly to `GET /api/courses`
- helps explain filtering plus ordering

SQL:

```sql
SELECT id, course_code, name, credits
FROM courses
WHERE active = true
ORDER BY course_code;
```

PowerShell command:

```powershell
docker exec -i campusenroll-postgres psql -U campus -d campusenroll -c "EXPLAIN (ANALYZE, BUFFERS) SELECT id, course_code, name, credits FROM courses WHERE active = true ORDER BY course_code;"
```

Relevant indexes from `db/schema.sql`:

- `idx_courses_active ON courses (active)`
- `uq_courses_course_code_ci ON courses (LOWER(course_code))`

Interpretation guidance:

- `Seq Scan on courses`
  - acceptable with the tiny seed dataset
- `Index Scan using idx_courses_active`
  - reasonable when the active filter becomes selective
- separate `Sort`
  - possible because there is no composite `(active, course_code)` index

## Representative query B: sections for the active academic period

Purpose:

- matches the course catalog and academic-period story
- exercises a join plus active-state filtering

SQL:

```sql
SELECT
    s.id,
    s.section_code,
    s.capacity,
    s.course_id,
    s.academic_period_id
FROM sections s
JOIN academic_periods ap
    ON ap.id = s.academic_period_id
WHERE s.active = true
  AND ap.active = true
ORDER BY s.section_code;
```

PowerShell command:

```powershell
docker exec -i campusenroll-postgres psql -U campus -d campusenroll -c "EXPLAIN (ANALYZE, BUFFERS) SELECT s.id, s.section_code, s.capacity, s.course_id, s.academic_period_id FROM sections s JOIN academic_periods ap ON ap.id = s.academic_period_id WHERE s.active = true AND ap.active = true ORDER BY s.section_code;"
```

Relevant indexes:

- `idx_sections_academic_period_id ON sections (academic_period_id)`
- `idx_sections_active ON sections (active)`
- `idx_academic_periods_active ON academic_periods (active)`

Interpretation guidance:

- a small-data `Hash Join` or `Nested Loop` is both normal and defensible
- `idx_sections_academic_period_id` becomes more useful as the number of sections grows
- `idx_academic_periods_active` helps when the active-period filter is selective

## Representative query C: enrollment plus billing join

Purpose:

- matches the academic-to-financial relationship in the project
- demonstrates why the foreign-key join path matters

SQL:

```sql
SELECT
    e.id AS enrollment_id,
    e.student_id,
    e.section_id,
    e.status AS enrollment_status,
    b.id AS billing_id,
    b.status AS billing_status,
    b.amount,
    b.created_at
FROM enrollments e
JOIN billings b
    ON b.enrollment_id = e.id
ORDER BY b.created_at DESC;
```

PowerShell command:

```powershell
docker exec -i campusenroll-postgres psql -U campus -d campusenroll -c "EXPLAIN (ANALYZE, BUFFERS) SELECT e.id AS enrollment_id, e.student_id, e.section_id, e.status AS enrollment_status, b.id AS billing_id, b.status AS billing_status, b.amount, b.created_at FROM enrollments e JOIN billings b ON b.enrollment_id = e.id ORDER BY b.created_at DESC;"
```

Relevant indexes:

- `idx_billings_enrollment_id ON billings (enrollment_id)`
- `idx_billings_created_at ON billings (created_at DESC)`
- `idx_enrollments_status ON enrollments (status)`

Interpretation guidance:

- for very small data, a `Hash Join` plus sort may still be chosen
- the important design point is that `billings.enrollment_id` is indexed for the join path
- `idx_billings_created_at` supports recency-oriented listing patterns

## Representative query D: duplicate-active enrollment protection

Purpose:

- maps directly to the concurrency protection in `enrollment-service`
- demonstrates how relational rules support anti-duplicate behavior

SQL:

```sql
SELECT id
FROM enrollments
WHERE student_id = 1
  AND section_id = 2
  AND status = 'ENROLLED';
```

PowerShell command:

```powershell
docker exec -i campusenroll-postgres psql -U campus -d campusenroll -c "EXPLAIN (ANALYZE, BUFFERS) SELECT id FROM enrollments WHERE student_id = 1 AND section_id = 2 AND status = 'ENROLLED';"
```

Relevant index:

- `uq_enrollments_active_student_section ON enrollments (student_id, section_id) WHERE status = 'ENROLLED'`

Interpretation guidance:

- this partial unique index is the strongest relational evidence for duplicate-active protection
- even if the tiny dataset still produces a small scan, the index definition itself is course-relevant evidence
- the index matters most when concurrent requests compete for the same student-section pair

## Optional inspection commands for indexes

Show all user-table indexes:

```powershell
docker exec -i campusenroll-postgres psql -U campus -d campusenroll -c "SELECT tablename, indexname, indexdef FROM pg_indexes WHERE schemaname = 'public' ORDER BY tablename, indexname;"
```

Focus on the most relevant tables:

```powershell
docker exec -i campusenroll-postgres psql -U campus -d campusenroll -c "SELECT tablename, indexname, indexdef FROM pg_indexes WHERE schemaname = 'public' AND tablename IN ('courses','academic_periods','sections','enrollments','billings') ORDER BY tablename, indexname;"
```

## How to read common plan elements

### `Seq Scan`

Meaning:

- PostgreSQL reads rows by scanning the table directly

When it is acceptable here:

- tiny demo tables
- low selectivity
- planner decides index setup cost is not worth it

### `Index Scan`

Meaning:

- PostgreSQL uses an index to locate matching rows

Why it matters:

- shows the index is aligned with the filter or join path
- becomes more valuable as row counts increase

### `Bitmap Index Scan` and `Bitmap Heap Scan`

Meaning:

- PostgreSQL uses an index to identify candidate pages, then fetches rows from the heap

Why it matters:

- often appears when multiple matching rows are expected
- can be a good middle ground between full table scan and direct index scan

### `Buffers: shared hit`

Meaning:

- data was already available in PostgreSQL shared buffers

Interpretation:

- good sign for repeated reads
- indicates memory reuse, not disk access

### `Buffers: shared read`

Meaning:

- PostgreSQL had to read blocks from disk into shared buffers

Interpretation:

- more expensive than a cache hit
- useful when discussing CPU versus I/O behavior

### `Planning Time`

Meaning:

- time spent choosing the execution plan

Interpretation:

- normally much smaller than execution time
- still worth mentioning in query-plan analysis

### `Execution Time`

Meaning:

- actual runtime for the query execution

Interpretation:

- best discussed together with row counts, join type, and buffer stats

## Index rationale from the real schema

The current index set in `db/schema.sql` supports four main needs:

### 1. Active-state filtering

Examples:

- `idx_students_active`
- `idx_courses_active`
- `idx_academic_periods_active`
- `idx_sections_active`
- `idx_enrollments_status`
- `idx_billings_status`

Why:

- many academic queries naturally filter active or current records

### 2. Foreign-key and join paths

Examples:

- `idx_sections_course_id`
- `idx_sections_academic_period_id`
- `idx_schedule_blocks_section_id`
- `idx_enrollments_student_id`
- `idx_enrollments_section_id`
- `idx_billings_enrollment_id`

Why:

- these keep join-heavy reads and relationship lookups defensible as the dataset grows

### 3. Time-oriented listing

Examples:

- `idx_enrollments_enrolled_at`
- `idx_billings_created_at`

Why:

- useful for chronological listing, evidence review, and operational inspection

### 4. Relational rule enforcement

Examples:

- `uq_courses_course_code_ci`
- `uq_enrollments_active_student_section`
- `uq_billings_pending_enrollment`

Why:

- these are not only performance objects
- they also encode business rules directly at the relational layer

## Suggested presentation wording

Use wording like this:

`CampusEnroll HA already includes real PostgreSQL indexes and partial unique indexes. During the presentation we can show the plan shape with EXPLAIN ANALYZE BUFFERS, but we also explain that the demo dataset is intentionally small, so PostgreSQL may still choose sequential scans in some cases. That does not invalidate the index design. It reflects planner cost decisions for a tiny dataset.`

## Bottom line

This file is evidence guidance, not a claim that every plan was already captured in advance.

Its value is that it gives the team a clean, repeatable, Database II aligned way to show:

- why query plans matter
- how the current schema supports the workload
- how to interpret PostgreSQL behavior honestly during the final presentation
