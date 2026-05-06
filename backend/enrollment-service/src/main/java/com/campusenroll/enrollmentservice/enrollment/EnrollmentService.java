package com.campusenroll.enrollmentservice.enrollment;

import com.campusenroll.enrollmentservice.enrollment.dto.CreateEnrollmentRequest;
import com.campusenroll.enrollmentservice.enrollment.dto.EnrollmentResponse;
import com.campusenroll.enrollmentservice.enrollment.dto.UpdateEnrollmentStatusRequest;
import com.campusenroll.enrollmentservice.error.ConflictException;
import com.campusenroll.enrollmentservice.error.ResourceNotFoundException;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;

    public EnrollmentService(EnrollmentRepository enrollmentRepository) {
        this.enrollmentRepository = enrollmentRepository;
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getAll() {
        return enrollmentRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public EnrollmentResponse getById(Long id) {
        return toResponse(findEnrollment(id));
    }

    @Transactional
    public EnrollmentResponse create(CreateEnrollmentRequest request) {
        if (enrollmentRepository.existsByStudentIdAndSectionIdAndStatus(
                request.getStudentId(), request.getSectionId(), EnrollmentStatus.ENROLLED)) {
            throw new ConflictException("An active enrollment already exists for this student and section");
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setStudentId(request.getStudentId());
        enrollment.setSectionId(request.getSectionId());
        enrollment.setStatus(EnrollmentStatus.ENROLLED);
        enrollment.setEnrolledAt(OffsetDateTime.now());

        try {
            return toResponse(enrollmentRepository.save(enrollment));
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("An enrollment already exists for this student and section");
        }
    }

    @Transactional
    public EnrollmentResponse updateStatus(Long id, UpdateEnrollmentStatusRequest request) {
        Enrollment enrollment = findEnrollment(id);
        EnrollmentStatus nextStatus = request.getStatus();

        if (nextStatus == EnrollmentStatus.ENROLLED
                && enrollment.getStatus() != EnrollmentStatus.ENROLLED
                && enrollmentRepository.existsByStudentIdAndSectionIdAndStatus(
                        enrollment.getStudentId(), enrollment.getSectionId(), EnrollmentStatus.ENROLLED)) {
            throw new ConflictException("An active enrollment already exists for this student and section");
        }

        enrollment.setStatus(nextStatus);

        try {
            return toResponse(enrollmentRepository.save(enrollment));
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("An enrollment already exists for this student and section");
        }
    }

    private Enrollment findEnrollment(Long id) {
        return enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));
    }

    private EnrollmentResponse toResponse(Enrollment enrollment) {
        return new EnrollmentResponse(
                enrollment.getId(),
                enrollment.getStudentId(),
                enrollment.getSectionId(),
                enrollment.getStatus(),
                enrollment.getEnrolledAt());
    }
}
