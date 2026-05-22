package com.campusenroll.billing.billing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.campusenroll.billing.billing.dto.BillingResponse;
import com.campusenroll.billing.billing.dto.CreateBillingRequest;
import com.campusenroll.billing.billing.dto.UpdateBillingStatusRequest;
import com.campusenroll.billing.error.ConflictException;
import com.campusenroll.billing.error.ResourceNotFoundException;
import com.campusenroll.billing.idempotency.IdempotencyRecord;
import com.campusenroll.billing.idempotency.IdempotencyRecordRepository;
import com.campusenroll.billing.idempotency.IdempotencyRecordStatus;
import com.campusenroll.billing.idempotency.IdempotencyService;
import com.campusenroll.billing.messaging.BillingEventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class BillingServiceTest {

    @Test
    void shouldListBillingsOrderedByCreatedAtDescThenIdDesc() {
        RepositoryState state = new RepositoryState();
        BillingService billingService = new BillingService(
                repository(state),
                new RecordingBillingEventPublisher(),
                idempotencyService(new IdempotencyRepositoryState()));

        Billing older = billing(100L, "99.99", "USD", BillingStatus.PENDING, OffsetDateTime.parse("2026-05-05T10:15:30Z"));
        Billing newer = billing(101L, "150.75", "USD", BillingStatus.PAID, OffsetDateTime.parse("2026-05-06T10:15:30Z"));
        Billing sameTimestampHigherId =
                billing(102L, "175.00", "USD", BillingStatus.CANCELLED, OffsetDateTime.parse("2026-05-06T10:15:30Z"));

        persist(state, older);
        persist(state, newer);
        persist(state, sameTimestampHigherId);

        List<BillingResponse> response = billingService.getAll();

        assertThat(response).extracting(BillingResponse::id).containsExactly(
                sameTimestampHigherId.getId(),
                newer.getId(),
                older.getId());
    }

    @Test
    void shouldCreateBilling() {
        RepositoryState state = new RepositoryState();
        RecordingBillingEventPublisher eventPublisher = new RecordingBillingEventPublisher();
        BillingService billingService = new BillingService(
                repository(state),
                eventPublisher,
                idempotencyService(new IdempotencyRepositoryState()));

        CreateBillingRequest request = new CreateBillingRequest();
        request.setEnrollmentId(100L);
        request.setAmount(new BigDecimal("150.75"));
        request.setCurrency("usd");
        request.setStatus(BillingStatus.PENDING);

        BillingResponse response = billingService.create(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.enrollmentId()).isEqualTo(100L);
        assertThat(response.amount()).isEqualByComparingTo("150.75");
        assertThat(response.currency()).isEqualTo("USD");
        assertThat(response.status()).isEqualTo(BillingStatus.PENDING);
        assertThat(response.createdAt()).isNotNull();
        assertThat(state.storage.values()).hasSize(1);
        assertThat(new ArrayList<>(state.storage.values()).get(0).getEnrollmentId()).isEqualTo(100L);
        assertThat(eventPublisher.publishedEvents).isEmpty();
    }

    @Test
    void shouldReplayCompletedBillingForSameIdempotencyKeyWithoutCreatingDuplicateRow() {
        RepositoryState state = new RepositoryState();
        IdempotencyRepositoryState idempotencyState = new IdempotencyRepositoryState();
        BillingService billingService = new BillingService(
                repository(state),
                new RecordingBillingEventPublisher(),
                idempotencyService(idempotencyState));

        CreateBillingRequest request = new CreateBillingRequest();
        request.setEnrollmentId(100L);
        request.setAmount(new BigDecimal("150.75"));
        request.setCurrency("USD");
        request.setStatus(BillingStatus.PENDING);

        var firstResponse = billingService.create(request, "billing-idem-1");
        var replayedResponse = billingService.create(request, "billing-idem-1");

        assertThat(firstResponse.status()).isEqualTo(201);
        assertThat(replayedResponse.status()).isEqualTo(201);
        assertThat(replayedResponse.body().id()).isEqualTo(firstResponse.body().id());
        assertThat(replayedResponse.body().enrollmentId()).isEqualTo(firstResponse.body().enrollmentId());
        assertThat(replayedResponse.body().amount()).isEqualByComparingTo(firstResponse.body().amount());
        assertThat(replayedResponse.body().currency()).isEqualTo(firstResponse.body().currency());
        assertThat(replayedResponse.body().status()).isEqualTo(firstResponse.body().status());
        assertThat(replayedResponse.body().createdAt().toInstant())
                .isEqualTo(firstResponse.body().createdAt().toInstant());
        assertThat(state.storage).hasSize(1);
        assertThat(idempotencyState.storage).hasSize(1);
        IdempotencyRecord storedRecord = new ArrayList<>(idempotencyState.storage.values()).get(0);
        assertThat(storedRecord.getStatus()).isEqualTo(IdempotencyRecordStatus.COMPLETED);
        assertThat(storedRecord.getResponseStatus()).isEqualTo(201);
    }

    @Test
    void shouldRejectBillingIdempotencyKeyReuseWithDifferentPayload() {
        RepositoryState state = new RepositoryState();
        IdempotencyRepositoryState idempotencyState = new IdempotencyRepositoryState();
        BillingService billingService = new BillingService(
                repository(state),
                new RecordingBillingEventPublisher(),
                idempotencyService(idempotencyState));

        CreateBillingRequest firstRequest = new CreateBillingRequest();
        firstRequest.setEnrollmentId(100L);
        firstRequest.setAmount(new BigDecimal("150.75"));
        firstRequest.setCurrency("USD");
        firstRequest.setStatus(BillingStatus.PENDING);
        billingService.create(firstRequest, "billing-idem-2");

        CreateBillingRequest secondRequest = new CreateBillingRequest();
        secondRequest.setEnrollmentId(101L);
        secondRequest.setAmount(new BigDecimal("175.00"));
        secondRequest.setCurrency("USD");
        secondRequest.setStatus(BillingStatus.PENDING);

        assertThatThrownBy(() -> billingService.create(secondRequest, "billing-idem-2"))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Idempotency key was reused with a different payload");
        assertThat(state.storage).hasSize(1);
    }

    @Test
    void shouldRejectDuplicatePendingBillingOnCreate() {
        RepositoryState state = new RepositoryState();
        BillingService billingService = new BillingService(
                repository(state),
                new RecordingBillingEventPublisher(),
                idempotencyService(new IdempotencyRepositoryState()));

        Billing existing = new Billing();
        existing.setEnrollmentId(100L);
        existing.setAmount(new BigDecimal("99.99"));
        existing.setCurrency("USD");
        existing.setStatus(BillingStatus.PENDING);
        existing.setCreatedAt(OffsetDateTime.now().minusDays(1));
        persist(state, existing);

        CreateBillingRequest request = new CreateBillingRequest();
        request.setEnrollmentId(100L);
        request.setAmount(new BigDecimal("150.75"));
        request.setCurrency("USD");
        request.setStatus(BillingStatus.PENDING);

        assertThatThrownBy(() -> billingService.create(request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("An active billing already exists for this enrollment");
    }

    @Test
    void shouldAllowNonPendingBillingForSameEnrollment() {
        RepositoryState state = new RepositoryState();
        BillingService billingService = new BillingService(
                repository(state),
                new RecordingBillingEventPublisher(),
                idempotencyService(new IdempotencyRepositoryState()));

        Billing existing = new Billing();
        existing.setEnrollmentId(100L);
        existing.setAmount(new BigDecimal("99.99"));
        existing.setCurrency("USD");
        existing.setStatus(BillingStatus.PENDING);
        existing.setCreatedAt(OffsetDateTime.now().minusDays(1));
        persist(state, existing);

        CreateBillingRequest request = new CreateBillingRequest();
        request.setEnrollmentId(100L);
        request.setAmount(new BigDecimal("150.75"));
        request.setCurrency("USD");
        request.setStatus(BillingStatus.PAID);

        BillingResponse response = billingService.create(request);

        assertThat(response.status()).isEqualTo(BillingStatus.PAID);
        assertThat(state.storage.get(response.id()).getStatus()).isEqualTo(BillingStatus.PAID);
    }

    @Test
    void shouldRejectMissingBillingOnStatusUpdate() {
        BillingService billingService = new BillingService(
                repository(new RepositoryState()),
                new RecordingBillingEventPublisher(),
                idempotencyService(new IdempotencyRepositoryState()));

        UpdateBillingStatusRequest request = new UpdateBillingStatusRequest();
        request.setStatus(BillingStatus.CANCELLED);

        assertThatThrownBy(() -> billingService.updateStatus(99L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Billing not found");
    }

    @Test
    void shouldRejectStatusChangeToPendingWhenAnotherPendingBillingExists() {
        RepositoryState state = new RepositoryState();
        BillingService billingService = new BillingService(
                repository(state),
                new RecordingBillingEventPublisher(),
                idempotencyService(new IdempotencyRepositoryState()));

        Billing target = new Billing();
        target.setEnrollmentId(100L);
        target.setAmount(new BigDecimal("150.75"));
        target.setCurrency("USD");
        target.setStatus(BillingStatus.CANCELLED);
        target.setCreatedAt(OffsetDateTime.now().minusDays(1));
        persist(state, target);

        Billing existing = new Billing();
        existing.setEnrollmentId(100L);
        existing.setAmount(new BigDecimal("99.99"));
        existing.setCurrency("USD");
        existing.setStatus(BillingStatus.PENDING);
        existing.setCreatedAt(OffsetDateTime.now().minusHours(1));
        persist(state, existing);

        UpdateBillingStatusRequest request = new UpdateBillingStatusRequest();
        request.setStatus(BillingStatus.PENDING);

        assertThatThrownBy(() -> billingService.updateStatus(target.getId(), request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("An active billing already exists for this enrollment");
    }

    @Test
    void shouldKeepBillingListCompatibleWhenStatusChangesFromPending() {
        RepositoryState state = new RepositoryState();
        RecordingBillingEventPublisher eventPublisher = new RecordingBillingEventPublisher();
        BillingService billingService = new BillingService(
                repository(state),
                eventPublisher,
                idempotencyService(new IdempotencyRepositoryState()));

        Billing existing = new Billing();
        existing.setEnrollmentId(100L);
        existing.setAmount(new BigDecimal("99.99"));
        existing.setCurrency("USD");
        existing.setStatus(BillingStatus.PENDING);
        existing.setCreatedAt(OffsetDateTime.now().minusHours(1));
        persist(state, existing);

        UpdateBillingStatusRequest request = new UpdateBillingStatusRequest();
        request.setStatus(BillingStatus.PAID);

        BillingResponse response = billingService.updateStatus(existing.getId(), request);

        assertThat(response.status()).isEqualTo(BillingStatus.PAID);
        assertThat(state.storage.get(existing.getId()).getStatus()).isEqualTo(BillingStatus.PAID);
        assertThat(eventPublisher.publishedEvents).hasSize(1);
        assertThat(eventPublisher.publishedEvents.get(0).previousStatus).isEqualTo(BillingStatus.PENDING);
        assertThat(eventPublisher.publishedEvents.get(0).newStatus).isEqualTo(BillingStatus.PAID);
    }

    @Test
    void shouldNotPublishEventWhenStatusDoesNotChange() {
        RepositoryState state = new RepositoryState();
        RecordingBillingEventPublisher eventPublisher = new RecordingBillingEventPublisher();
        BillingService billingService = new BillingService(
                repository(state),
                eventPublisher,
                idempotencyService(new IdempotencyRepositoryState()));

        Billing existing = new Billing();
        existing.setEnrollmentId(100L);
        existing.setAmount(new BigDecimal("99.99"));
        existing.setCurrency("USD");
        existing.setStatus(BillingStatus.PENDING);
        existing.setCreatedAt(OffsetDateTime.now().minusHours(1));
        persist(state, existing);

        UpdateBillingStatusRequest request = new UpdateBillingStatusRequest();
        request.setStatus(BillingStatus.PENDING);

        BillingResponse response = billingService.updateStatus(existing.getId(), request);

        assertThat(response.status()).isEqualTo(BillingStatus.PENDING);
        assertThat(eventPublisher.publishedEvents).isEmpty();
    }

    private static BillingRepository repository(RepositoryState state) {
        InvocationHandler handler = new BillingRepositoryHandler(state);
        return (BillingRepository) Proxy.newProxyInstance(
                BillingRepository.class.getClassLoader(),
                new Class<?>[] {BillingRepository.class},
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

    private static Billing persist(RepositoryState state, Billing billing) {
        if (billing.getId() == null) {
            billing.setId(state.sequence++);
        }
        state.storage.put(billing.getId(), billing);
        return billing;
    }

    private static Billing billing(
            Long enrollmentId,
            String amount,
            String currency,
            BillingStatus status,
            OffsetDateTime createdAt) {
        Billing billing = new Billing();
        billing.setEnrollmentId(enrollmentId);
        billing.setAmount(new BigDecimal(amount));
        billing.setCurrency(currency);
        billing.setStatus(status);
        billing.setCreatedAt(createdAt);
        return billing;
    }

    private static final class RepositoryState {
        private final Map<Long, Billing> storage = new HashMap<>();
        private long sequence = 1L;
    }

    private static final class IdempotencyRepositoryState {
        private final Map<Long, IdempotencyRecord> storage = new HashMap<>();
        private long sequence = 1L;
    }

    private static final class RecordingBillingEventPublisher implements BillingEventPublisher {
        private final List<PublishedStatusChange> publishedEvents = new ArrayList<>();

        @Override
        public void publishBillingStatusChanged(Billing billing, BillingStatus previousStatus, BillingStatus newStatus) {
            publishedEvents.add(new PublishedStatusChange(billing.getId(), previousStatus, newStatus));
        }
    }

    private record PublishedStatusChange(Long billingId, BillingStatus previousStatus, BillingStatus newStatus) {}

    private static final class BillingRepositoryHandler implements InvocationHandler {

        private final RepositoryState state;

        private BillingRepositoryHandler(RepositoryState state) {
            this.state = state;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            return switch (method.getName()) {
                case "existsByEnrollmentIdAndStatus" -> state.storage.values().stream()
                        .anyMatch(billing -> billing.getEnrollmentId().equals(args[0]) && billing.getStatus() == args[1]);
                case "existsByEnrollmentIdAndStatusAndIdNot" -> state.storage.values().stream()
                        .anyMatch(billing -> billing.getEnrollmentId().equals(args[0])
                                && billing.getStatus() == args[1]
                                && !billing.getId().equals(args[2]));
                case "findAllByOrderByCreatedAtDescIdDesc" -> state.storage.values().stream()
                        .sorted(Comparator.comparing(Billing::getCreatedAt)
                                .reversed()
                                .thenComparing(Billing::getId, Comparator.reverseOrder()))
                        .toList();
                case "findById" -> Optional.ofNullable(state.storage.get(args[0]));
                case "save", "saveAndFlush" -> persist(state, (Billing) args[0]);
                case "toString" -> "BillingRepositoryTestProxy";
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> proxy == args[0];
                default -> throw new UnsupportedOperationException("Unsupported repository method: " + method.getName());
            };
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
                case "toString" -> "BillingIdempotencyRecordRepositoryTestProxy";
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
                case "toString" -> "BillingIdempotencyEntityManagerTestProxy";
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> proxy == args[0];
                default -> throw new UnsupportedOperationException("Unsupported entity manager method: " + method.getName());
            };
        }
    }

    private static Object nativeQueryProxy(IdempotencyRepositoryState state) {
        InvocationHandler handler = new NativeQueryHandler(state);
        return Proxy.newProxyInstance(
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
                case "toString" -> "BillingIdempotencyNativeQueryTestProxy";
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
