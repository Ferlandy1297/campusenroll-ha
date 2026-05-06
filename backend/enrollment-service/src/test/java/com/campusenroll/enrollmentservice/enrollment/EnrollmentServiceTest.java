package com.campusenroll.enrollmentservice.enrollment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.campusenroll.enrollmentservice.enrollment.dto.CreateEnrollmentRequest;
import com.campusenroll.enrollmentservice.enrollment.dto.EnrollmentResponse;
import com.campusenroll.enrollmentservice.enrollment.dto.UpdateEnrollmentStatusRequest;
import com.campusenroll.enrollmentservice.error.ConflictException;
import com.campusenroll.enrollmentservice.error.ResourceNotFoundException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class EnrollmentServiceTest {

    @Test
    void shouldCreateEnrollmentWithDefaultEnrolledStatus() {
        RepositoryState state = new RepositoryState();
        EnrollmentService enrollmentService = new EnrollmentService(repository(state));

        CreateEnrollmentRequest request = new CreateEnrollmentRequest();
        request.setStudentId(100L);
        request.setSectionId(200L);

        EnrollmentResponse response = enrollmentService.create(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.studentId()).isEqualTo(100L);
        assertThat(response.sectionId()).isEqualTo(200L);
        assertThat(response.status()).isEqualTo(EnrollmentStatus.ENROLLED);
        assertThat(response.enrolledAt()).isNotNull();
        assertThat(state.storage.values()).hasSize(1);
        assertThat(new ArrayList<>(state.storage.values()).get(0).getEnrolledAt()).isNotNull();
    }

    @Test
    void shouldRejectDuplicateActiveEnrollmentOnCreate() {
        RepositoryState state = new RepositoryState();
        EnrollmentService enrollmentService = new EnrollmentService(repository(state));

        Enrollment existing = new Enrollment();
        existing.setStudentId(100L);
        existing.setSectionId(200L);
        existing.setStatus(EnrollmentStatus.ENROLLED);
        existing.setEnrolledAt(OffsetDateTime.now().minusDays(1));
        persist(state, existing);

        CreateEnrollmentRequest request = new CreateEnrollmentRequest();
        request.setStudentId(100L);
        request.setSectionId(200L);

        assertThatThrownBy(() -> enrollmentService.create(request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("An active enrollment already exists for this student and section");
    }

    @Test
    void shouldRejectMissingEnrollmentOnStatusUpdate() {
        EnrollmentService enrollmentService = new EnrollmentService(repository(new RepositoryState()));

        UpdateEnrollmentStatusRequest request = new UpdateEnrollmentStatusRequest();
        request.setStatus(EnrollmentStatus.CANCELLED);

        assertThatThrownBy(() -> enrollmentService.updateStatus(99L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Enrollment not found");
    }

    @Test
    void shouldRejectReactivationWhenAnotherActiveEnrollmentExists() {
        RepositoryState state = new RepositoryState();
        EnrollmentService enrollmentService = new EnrollmentService(repository(state));

        Enrollment existing = new Enrollment();
        existing.setStudentId(100L);
        existing.setSectionId(200L);
        existing.setStatus(EnrollmentStatus.CANCELLED);
        existing.setEnrolledAt(OffsetDateTime.now().minusDays(1));
        persist(state, existing);

        Enrollment duplicate = new Enrollment();
        duplicate.setStudentId(100L);
        duplicate.setSectionId(200L);
        duplicate.setStatus(EnrollmentStatus.ENROLLED);
        duplicate.setEnrolledAt(OffsetDateTime.now().minusHours(1));
        persist(state, duplicate);

        UpdateEnrollmentStatusRequest request = new UpdateEnrollmentStatusRequest();
        request.setStatus(EnrollmentStatus.ENROLLED);

        assertThatThrownBy(() -> enrollmentService.updateStatus(existing.getId(), request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("An active enrollment already exists for this student and section");
    }

    private static EnrollmentRepository repository(RepositoryState state) {
        InvocationHandler handler = new EnrollmentRepositoryHandler(state);
        return (EnrollmentRepository) Proxy.newProxyInstance(
                EnrollmentRepository.class.getClassLoader(),
                new Class<?>[] {EnrollmentRepository.class},
                handler);
    }

    private static Enrollment persist(RepositoryState state, Enrollment enrollment) {
        if (enrollment.getId() == null) {
            enrollment.setId(state.sequence++);
        }
        state.storage.put(enrollment.getId(), enrollment);
        return enrollment;
    }

    private static final class RepositoryState {
        private final Map<Long, Enrollment> storage = new HashMap<>();
        private long sequence = 1L;
    }

    private static final class EnrollmentRepositoryHandler implements InvocationHandler {

        private final RepositoryState state;

        private EnrollmentRepositoryHandler(RepositoryState state) {
            this.state = state;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            return switch (method.getName()) {
                case "existsByStudentIdAndSectionIdAndStatus" -> state.storage.values().stream()
                        .anyMatch(enrollment -> enrollment.getStudentId().equals(args[0])
                                && enrollment.getSectionId().equals(args[1])
                                && enrollment.getStatus() == args[2]);
                case "findAll" -> new ArrayList<>(state.storage.values());
                case "findById" -> Optional.ofNullable(state.storage.get(args[0]));
                case "save" -> persist(state, (Enrollment) args[0]);
                case "toString" -> "EnrollmentRepositoryTestProxy";
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> proxy == args[0];
                default -> throw new UnsupportedOperationException("Unsupported repository method: " + method.getName());
            };
        }
    }
}
