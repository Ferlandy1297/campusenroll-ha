package com.campusenroll.enrollmentservice.enrollment;

import com.campusenroll.enrollmentservice.enrollment.dto.CreateEnrollmentRequest;
import com.campusenroll.enrollmentservice.enrollment.dto.EnrollmentResponse;
import com.campusenroll.enrollmentservice.enrollment.dto.UpdateEnrollmentStatusRequest;
import com.campusenroll.enrollmentservice.messaging.EnrollmentEventPublisher;
import com.campusenroll.enrollmentservice.error.ConflictException;
import com.campusenroll.enrollmentservice.error.ResourceNotFoundException;
import com.campusenroll.enrollmentservice.idempotency.IdempotentResponse;
import com.campusenroll.enrollmentservice.idempotency.IdempotencyService;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EnrollmentService {

    private static final String DUPLICATE_ACTIVE_ENROLLMENT_MESSAGE =
            "An active enrollment already exists for this student and section";
    private static final String SERVICE_NAME = "enrollment-service";
    private static final String CREATE_OPERATION = "create-enrollment";

    private final EnrollmentRepository enrollmentRepository;
    private final EnrollmentEventPublisher enrollmentEventPublisher;
    private final IdempotencyService idempotencyService;

    public EnrollmentService(
            EnrollmentRepository enrollmentRepository,
            EnrollmentEventPublisher enrollmentEventPublisher,
            IdempotencyService idempotencyService) {
        this.enrollmentRepository = enrollmentRepository;
        this.enrollmentEventPublisher = enrollmentEventPublisher;
        this.idempotencyService = idempotencyService;
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
        return createEnrollment(request);
    }

    @Transactional
    public IdempotentResponse<EnrollmentResponse> create(CreateEnrollmentRequest request, String idempotencyKey) {
        return idempotencyService.execute(
                SERVICE_NAME,
                CREATE_OPERATION,
                idempotencyKey,
                request,
                EnrollmentResponse.class,
                () -> IdempotentResponse.created(createEnrollment(request)));
    }

    private EnrollmentResponse createEnrollment(CreateEnrollmentRequest request) {
        if (enrollmentRepository.existsByStudentIdAndSectionIdAndStatus(
                request.getStudentId(), request.getSectionId(), EnrollmentStatus.ENROLLED)) {
            throw new ConflictException(DUPLICATE_ACTIVE_ENROLLMENT_MESSAGE);
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setStudentId(request.getStudentId());
        enrollment.setSectionId(request.getSectionId());
        enrollment.setStatus(EnrollmentStatus.ENROLLED);
        enrollment.setEnrolledAt(OffsetDateTime.now());

        try {
            Enrollment savedEnrollment = enrollmentRepository.saveAndFlush(enrollment);
            enrollmentEventPublisher.publishEnrollmentCreated(savedEnrollment);
            return toResponse(savedEnrollment);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException(DUPLICATE_ACTIVE_ENROLLMENT_MESSAGE);
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
            throw new ConflictException(DUPLICATE_ACTIVE_ENROLLMENT_MESSAGE);
        }

        enrollment.setStatus(nextStatus);

        try {
            return toResponse(enrollmentRepository.saveAndFlush(enrollment));
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException(DUPLICATE_ACTIVE_ENROLLMENT_MESSAGE);
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
