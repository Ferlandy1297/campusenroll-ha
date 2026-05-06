package com.campusenroll.enrollmentservice.enrollment;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    boolean existsByStudentIdAndSectionIdAndStatus(Long studentId, Long sectionId, EnrollmentStatus status);
}
