package com.campusenroll.enrollmentservice.enrollment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.campusenroll.enrollmentservice.enrollment.dto.CreateEnrollmentRequest;
import com.campusenroll.enrollmentservice.enrollment.dto.EnrollmentResponse;
import com.campusenroll.enrollmentservice.enrollment.dto.UpdateEnrollmentStatusRequest;
import com.campusenroll.enrollmentservice.error.ConflictException;
import com.campusenroll.enrollmentservice.error.ResourceNotFoundException;
import com.campusenroll.enrollmentservice.idempotency.IdempotencyRecord;
import com.campusenroll.enrollmentservice.idempotency.IdempotencyRecordRepository;
import com.campusenroll.enrollmentservice.idempotency.IdempotencyRecordStatus;
import com.campusenroll.enrollmentservice.idempotency.IdempotencyService;
import com.campusenroll.enrollmentservice.messaging.EnrollmentEventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
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
import org.springframework.dao.DataIntegrityViolationException;

class EnrollmentServiceTest {

    @Test
    void shouldCreateEnrollmentWithDefaultEnrolledStatus() {
        RepositoryState state = new RepositoryState();
        RecordingEnrollmentEventPublisher eventPublisher = new RecordingEnrollmentEventPublisher();
        EnrollmentService enrollmentService = new EnrollmentService(
                repository(state),
                eventPublisher,
                idempotencyService(new IdempotencyRepositoryState()));

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
        assertThat(eventPublisher.publishedEnrollments).hasSize(1);
        assertThat(eventPublisher.publishedEnrollments.get(0).getId()).isEqualTo(1L);
    }

    @Test
    void shouldReplayCompletedEnrollmentForSameIdempotencyKeyWithoutPublishingDuplicateEvent() {
        RepositoryState state = new RepositoryState();
        IdempotencyRepositoryState idempotencyState = new IdempotencyRepositoryState();
        RecordingEnrollmentEventPublisher eventPublisher = new RecordingEnrollmentEventPublisher();
        EnrollmentService enrollmentService =
                new EnrollmentService(repository(state), eventPublisher, idempotencyService(idempotencyState));

        CreateEnrollmentRequest request = new CreateEnrollmentRequest();
        request.setStudentId(100L);
        request.setSectionId(200L);

        var firstResponse = enrollmentService.create(request, "enrollment-idem-1");
        var replayedResponse = enrollmentService.create(request, "enrollment-idem-1");

        assertThat(firstResponse.status()).isEqualTo(201);
        assertThat(replayedResponse.status()).isEqualTo(201);
        assertThat(replayedResponse.body().id()).isEqualTo(firstResponse.body().id());
        assertThat(replayedResponse.body().studentId()).isEqualTo(firstResponse.body().studentId());
        assertThat(replayedResponse.body().sectionId()).isEqualTo(firstResponse.body().sectionId());
        assertThat(replayedResponse.body().status()).isEqualTo(firstResponse.body().status());
        assertThat(replayedResponse.body().enrolledAt().toInstant())
                .isEqualTo(firstResponse.body().enrolledAt().toInstant());
        assertThat(state.storage).hasSize(1);
        assertThat(eventPublisher.publishedEnrollments).hasSize(1);
        assertThat(idempotencyState.storage).hasSize(1);
        IdempotencyRecord storedRecord = new ArrayList<>(idempotencyState.storage.values()).get(0);
        assertThat(storedRecord.getStatus()).isEqualTo(IdempotencyRecordStatus.COMPLETED);
        assertThat(storedRecord.getResponseStatus()).isEqualTo(201);
    }

    @Test
    void shouldRejectIdempotencyKeyReuseWithDifferentPayload() {
        RepositoryState state = new RepositoryState();
        IdempotencyRepositoryState idempotencyState = new IdempotencyRepositoryState();
        EnrollmentService enrollmentService = new EnrollmentService(
                repository(state),
                new RecordingEnrollmentEventPublisher(),
                idempotencyService(idempotencyState));

        CreateEnrollmentRequest firstRequest = new CreateEnrollmentRequest();
        firstRequest.setStudentId(100L);
        firstRequest.setSectionId(200L);
        enrollmentService.create(firstRequest, "enrollment-idem-2");

        CreateEnrollmentRequest secondRequest = new CreateEnrollmentRequest();
        secondRequest.setStudentId(100L);
        secondRequest.setSectionId(201L);

        assertThatThrownBy(() -> enrollmentService.create(secondRequest, "enrollment-idem-2"))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Idempotency key was reused with a different payload");
        assertThat(state.storage).hasSize(1);
    }

    @Test
    void shouldRejectDuplicateActiveEnrollmentOnCreate() {
        RepositoryState state = new RepositoryState();
        EnrollmentService enrollmentService = new EnrollmentService(
                repository(state),
                new RecordingEnrollmentEventPublisher(),
                idempotencyService(new IdempotencyRepositoryState()));

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
    void shouldNotPublishEventWhenDatabaseConstraintRejectsCreate() {
        RepositoryState state = new RepositoryState();
        state.saveAndFlushException = new DataIntegrityViolationException("duplicate active enrollment");
        RecordingEnrollmentEventPublisher eventPublisher = new RecordingEnrollmentEventPublisher();
        EnrollmentService enrollmentService = new EnrollmentService(
                repository(state),
                eventPublisher,
                idempotencyService(new IdempotencyRepositoryState()));

        CreateEnrollmentRequest request = new CreateEnrollmentRequest();
        request.setStudentId(100L);
        request.setSectionId(200L);

        assertThatThrownBy(() -> enrollmentService.create(request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("An active enrollment already exists for this student and section");
        assertThat(eventPublisher.publishedEnrollments).isEmpty();
        assertThat(state.storage).isEmpty();
    }

    @Test
    void shouldRejectMissingEnrollmentOnStatusUpdate() {
        EnrollmentService enrollmentService = new EnrollmentService(
                repository(new RepositoryState()),
                new RecordingEnrollmentEventPublisher(),
                idempotencyService(new IdempotencyRepositoryState()));

        UpdateEnrollmentStatusRequest request = new UpdateEnrollmentStatusRequest();
        request.setStatus(EnrollmentStatus.CANCELLED);

        assertThatThrownBy(() -> enrollmentService.updateStatus(99L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Enrollment not found");
    }

    @Test
    void shouldRejectReactivationWhenAnotherActiveEnrollmentExists() {
        RepositoryState state = new RepositoryState();
        EnrollmentService enrollmentService = new EnrollmentService(
                repository(state),
                new RecordingEnrollmentEventPublisher(),
                idempotencyService(new IdempotencyRepositoryState()));

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

    private static IdempotencyService idempotencyService(IdempotencyRepositoryState state) {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        return new IdempotencyService(idempotencyRepository(state), objectMapper, entityManager(state));
    }

    private static IdempotencyRecordRepository idempotencyRepository(IdempotencyRepositoryState state) {
        InvocationHandler handler = new IdempotencyRepositoryHandler(state);
        return (IdempotencyRecordRepository) Proxy.newProxyInstance(
                IdempotencyRecordRepository.class.getClassLoader(),
                new Class<?>[] {IdempotencyRecordRepository.class},
                handler);
    }

    private static EntityManager entityManager(IdempotencyRepositoryState state) {
        InvocationHandler handler = new IdempotencyEntityManagerHandler(state);
        return (EntityManager) Proxy.newProxyInstance(
                EntityManager.class.getClassLoader(),
                new Class<?>[] {EntityManager.class},
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
        private RuntimeException saveAndFlushException;
    }

    private static final class IdempotencyRepositoryState {
        private final Map<Long, IdempotencyRecord> storage = new HashMap<>();
        private long sequence = 1L;
    }

    private static final class RecordingEnrollmentEventPublisher implements EnrollmentEventPublisher {
        private final List<Enrollment> publishedEnrollments = new ArrayList<>();

        @Override
        public void publishEnrollmentCreated(Enrollment enrollment) {
            publishedEnrollments.add(enrollment);
        }
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
                case "save", "saveAndFlush" -> save((Enrollment) args[0]);
                case "toString" -> "EnrollmentRepositoryTestProxy";
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> proxy == args[0];
                default -> throw new UnsupportedOperationException("Unsupported repository method: " + method.getName());
            };
        }

        private Enrollment save(Enrollment enrollment) {
            if (state.saveAndFlushException != null) {
                throw state.saveAndFlushException;
            }
            return persist(state, enrollment);
        }
    }

    private static final class IdempotencyRepositoryHandler implements InvocationHandler {

        private final IdempotencyRepositoryState state;

        private IdempotencyRepositoryHandler(IdempotencyRepositoryState state) {
            this.state = state;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            return switch (method.getName()) {
                case "findByServiceNameAndOperationNameAndIdempotencyKey" -> state.storage.values().stream()
                        .filter(record -> record.getServiceName().equals(args[0])
                                && record.getOperationName().equals(args[1])
                                && record.getIdempotencyKey().equals(args[2]))
                        .findFirst();
                case "save", "saveAndFlush" -> save((IdempotencyRecord) args[0]);
                case "toString" -> "IdempotencyRecordRepositoryTestProxy";
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> proxy == args[0];
                default -> throw new UnsupportedOperationException("Unsupported repository method: " + method.getName());
            };
        }

        private IdempotencyRecord save(IdempotencyRecord record) {
            if (record.getId() == null) {
                record.setId(state.sequence++);
            }

            state.storage.put(record.getId(), record);
            return record;
        }
    }

    private static final class IdempotencyEntityManagerHandler implements InvocationHandler {

        private final IdempotencyRepositoryState state;

        private IdempotencyEntityManagerHandler(IdempotencyRepositoryState state) {
            this.state = state;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            return switch (method.getName()) {
                case "createNativeQuery" -> nativeQueryProxy(state);
                case "toString" -> "EnrollmentIdempotencyEntityManagerTestProxy";
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> proxy == args[0];
                default -> throw new UnsupportedOperationException("Unsupported entity manager method: " + method.getName());
            };
        }
    }

    private static Query nativeQueryProxy(IdempotencyRepositoryState state) {
        InvocationHandler handler = new NativeQueryHandler(state);
        return (Query) Proxy.newProxyInstance(
                Query.class.getClassLoader(),
                new Class<?>[] {Query.class},
                handler);
    }

    private static final class NativeQueryHandler implements InvocationHandler {

        private final IdempotencyRepositoryState state;
        private final Map<String, Object> parameters = new HashMap<>();

        private NativeQueryHandler(IdempotencyRepositoryState state) {
            this.state = state;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            return switch (method.getName()) {
                case "setParameter" -> {
                    parameters.put((String) args[0], args[1]);
                    yield proxy;
                }
                case "executeUpdate" -> reserve();
                case "toString" -> "EnrollmentIdempotencyNativeQueryTestProxy";
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> proxy == args[0];
                default -> throw new UnsupportedOperationException("Unsupported native query method: " + method.getName());
            };
        }

        private int reserve() {
            boolean alreadyExists = state.storage.values().stream()
                    .anyMatch(record -> record.getServiceName().equals(parameters.get("serviceName"))
                            && record.getOperationName().equals(parameters.get("operationName"))
                            && record.getIdempotencyKey().equals(parameters.get("idempotencyKey")));

            if (alreadyExists) {
                return 0;
            }

            IdempotencyRecord record = new IdempotencyRecord();
            record.setId(state.sequence++);
            record.setServiceName((String) parameters.get("serviceName"));
            record.setOperationName((String) parameters.get("operationName"));
            record.setIdempotencyKey((String) parameters.get("idempotencyKey"));
            record.setRequestHash((String) parameters.get("requestHash"));
            record.setStatus(IdempotencyRecordStatus.valueOf((String) parameters.get("status")));
            record.setCreatedAt((OffsetDateTime) parameters.get("createdAt"));
            state.storage.put(record.getId(), record);
            return 1;
        }
    }
}
