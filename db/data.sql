-- CampusEnroll HA demo data
-- Small seed set for manual testing of the authoritative Base de Datos II schema.

BEGIN;

INSERT INTO students (id, student_code, first_name, last_name, email, active)
VALUES
    (1, 'STU-2026-001', 'Ana', 'Lopez', 'ana.lopez@campusenroll.edu', TRUE),
    (2, 'STU-2026-002', 'Luis', 'Mendez', 'luis.mendez@campusenroll.edu', TRUE),
    (3, 'STU-2026-003', 'Maria', 'Garcia', 'maria.garcia@campusenroll.edu', TRUE)
ON CONFLICT (id) DO UPDATE
SET
    student_code = EXCLUDED.student_code,
    first_name = EXCLUDED.first_name,
    last_name = EXCLUDED.last_name,
    email = EXCLUDED.email,
    active = EXCLUDED.active;

INSERT INTO courses (id, course_code, name, credits, active)
VALUES
    (1, 'CS101', 'Introduction to Programming', 4, TRUE),
    (2, 'DB201', 'Database Systems', 4, TRUE),
    (3, 'MAT150', 'Discrete Mathematics', 3, TRUE)
ON CONFLICT (id) DO UPDATE
SET
    course_code = EXCLUDED.course_code,
    name = EXCLUDED.name,
    credits = EXCLUDED.credits,
    active = EXCLUDED.active;

INSERT INTO academic_periods (id, name, active)
VALUES
    (1, '2026-A', TRUE),
    (2, '2026-B', FALSE)
ON CONFLICT (id) DO UPDATE
SET
    name = EXCLUDED.name,
    active = EXCLUDED.active;

INSERT INTO sections (id, section_code, capacity, active, course_id, academic_period_id)
VALUES
    (1, 'CS101-A', 30, TRUE, 1, 1),
    (2, 'DB201-A', 25, TRUE, 2, 1),
    (3, 'MAT150-B', 35, TRUE, 3, 2)
ON CONFLICT (id) DO UPDATE
SET
    section_code = EXCLUDED.section_code,
    capacity = EXCLUDED.capacity,
    active = EXCLUDED.active,
    course_id = EXCLUDED.course_id,
    academic_period_id = EXCLUDED.academic_period_id;

INSERT INTO schedule_blocks (id, section_id, block_order, day_of_week, start_time, end_time)
VALUES
    (1, 1, 0, 'MONDAY', '08:00:00', '09:30:00'),
    (2, 1, 1, 'WEDNESDAY', '08:00:00', '09:30:00'),
    (3, 2, 0, 'TUESDAY', '10:00:00', '11:30:00'),
    (4, 2, 1, 'THURSDAY', '10:00:00', '11:30:00'),
    (5, 3, 0, 'FRIDAY', '14:00:00', '16:00:00')
ON CONFLICT (id) DO UPDATE
SET
    section_id = EXCLUDED.section_id,
    block_order = EXCLUDED.block_order,
    day_of_week = EXCLUDED.day_of_week,
    start_time = EXCLUDED.start_time,
    end_time = EXCLUDED.end_time;

INSERT INTO enrollments (id, student_id, section_id, status, enrolled_at)
VALUES
    (1, 1, 1, 'ENROLLED', '2026-05-01 08:15:00+00'),
    (2, 2, 2, 'CANCELLED', '2026-05-02 10:30:00+00'),
    (3, 3, 3, 'ENROLLED', '2026-05-03 14:05:00+00')
ON CONFLICT (id) DO UPDATE
SET
    student_id = EXCLUDED.student_id,
    section_id = EXCLUDED.section_id,
    status = EXCLUDED.status,
    enrolled_at = EXCLUDED.enrolled_at;

INSERT INTO billings (id, enrollment_id, amount, currency, status, created_at)
VALUES
    (1, 1, 150.00, 'USD', 'PENDING', '2026-05-01 08:20:00+00'),
    (2, 2, 150.00, 'USD', 'CANCELLED', '2026-05-02 10:45:00+00'),
    (3, 3, 175.50, 'USD', 'PAID', '2026-05-03 14:15:00+00')
ON CONFLICT (id) DO UPDATE
SET
    enrollment_id = EXCLUDED.enrollment_id,
    amount = EXCLUDED.amount,
    currency = EXCLUDED.currency,
    status = EXCLUDED.status,
    created_at = EXCLUDED.created_at;

SELECT setval(pg_get_serial_sequence('students', 'id'), COALESCE((SELECT MAX(id) FROM students), 1), TRUE);
SELECT setval(pg_get_serial_sequence('courses', 'id'), COALESCE((SELECT MAX(id) FROM courses), 1), TRUE);
SELECT setval(pg_get_serial_sequence('academic_periods', 'id'), COALESCE((SELECT MAX(id) FROM academic_periods), 1), TRUE);
SELECT setval(pg_get_serial_sequence('sections', 'id'), COALESCE((SELECT MAX(id) FROM sections), 1), TRUE);
SELECT setval(pg_get_serial_sequence('schedule_blocks', 'id'), COALESCE((SELECT MAX(id) FROM schedule_blocks), 1), TRUE);
SELECT setval(pg_get_serial_sequence('enrollments', 'id'), COALESCE((SELECT MAX(id) FROM enrollments), 1), TRUE);
SELECT setval(pg_get_serial_sequence('billings', 'id'), COALESCE((SELECT MAX(id) FROM billings), 1), TRUE);

COMMIT;
