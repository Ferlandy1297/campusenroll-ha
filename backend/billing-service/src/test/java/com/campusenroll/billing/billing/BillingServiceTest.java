package com.campusenroll.billing.billing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.campusenroll.billing.billing.dto.BillingResponse;
import com.campusenroll.billing.billing.dto.CreateBillingRequest;
import com.campusenroll.billing.billing.dto.UpdateBillingStatusRequest;
import com.campusenroll.billing.error.ConflictException;
import com.campusenroll.billing.error.ResourceNotFoundException;
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
    void shouldCreateBilling() {
        RepositoryState state = new RepositoryState();
        BillingService billingService = new BillingService(repository(state));

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
        assertThat(new ArrayList<>(state.storage.values()).get(0).getPendingEnrollmentKey()).isEqualTo(100L);
    }

    @Test
    void shouldRejectDuplicatePendingBillingOnCreate() {
        RepositoryState state = new RepositoryState();
        BillingService billingService = new BillingService(repository(state));

        Billing existing = new Billing();
        existing.setEnrollmentId(100L);
        existing.setAmount(new BigDecimal("99.99"));
        existing.setCurrency("USD");
        existing.setStatus(BillingStatus.PENDING);
        existing.setCreatedAt(OffsetDateTime.now().minusDays(1));
        existing.syncPendingEnrollmentKey();
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
        BillingService billingService = new BillingService(repository(state));

        Billing existing = new Billing();
        existing.setEnrollmentId(100L);
        existing.setAmount(new BigDecimal("99.99"));
        existing.setCurrency("USD");
        existing.setStatus(BillingStatus.PENDING);
        existing.setCreatedAt(OffsetDateTime.now().minusDays(1));
        existing.syncPendingEnrollmentKey();
        persist(state, existing);

        CreateBillingRequest request = new CreateBillingRequest();
        request.setEnrollmentId(100L);
        request.setAmount(new BigDecimal("150.75"));
        request.setCurrency("USD");
        request.setStatus(BillingStatus.PAID);

        BillingResponse response = billingService.create(request);

        assertThat(response.status()).isEqualTo(BillingStatus.PAID);
        assertThat(state.storage.get(response.id()).getPendingEnrollmentKey()).isNull();
    }

    @Test
    void shouldRejectMissingBillingOnStatusUpdate() {
        BillingService billingService = new BillingService(repository(new RepositoryState()));

        UpdateBillingStatusRequest request = new UpdateBillingStatusRequest();
        request.setStatus(BillingStatus.CANCELLED);

        assertThatThrownBy(() -> billingService.updateStatus(99L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Billing not found");
    }

    @Test
    void shouldRejectStatusChangeToPendingWhenAnotherPendingBillingExists() {
        RepositoryState state = new RepositoryState();
        BillingService billingService = new BillingService(repository(state));

        Billing target = new Billing();
        target.setEnrollmentId(100L);
        target.setAmount(new BigDecimal("150.75"));
        target.setCurrency("USD");
        target.setStatus(BillingStatus.CANCELLED);
        target.setCreatedAt(OffsetDateTime.now().minusDays(1));
        target.syncPendingEnrollmentKey();
        persist(state, target);

        Billing existing = new Billing();
        existing.setEnrollmentId(100L);
        existing.setAmount(new BigDecimal("99.99"));
        existing.setCurrency("USD");
        existing.setStatus(BillingStatus.PENDING);
        existing.setCreatedAt(OffsetDateTime.now().minusHours(1));
        existing.syncPendingEnrollmentKey();
        persist(state, existing);

        UpdateBillingStatusRequest request = new UpdateBillingStatusRequest();
        request.setStatus(BillingStatus.PENDING);

        assertThatThrownBy(() -> billingService.updateStatus(target.getId(), request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("An active billing already exists for this enrollment");
    }

    @Test
    void shouldClearPendingEnrollmentKeyWhenStatusChangesFromPending() {
        RepositoryState state = new RepositoryState();
        BillingService billingService = new BillingService(repository(state));

        Billing existing = new Billing();
        existing.setEnrollmentId(100L);
        existing.setAmount(new BigDecimal("99.99"));
        existing.setCurrency("USD");
        existing.setStatus(BillingStatus.PENDING);
        existing.setCreatedAt(OffsetDateTime.now().minusHours(1));
        existing.syncPendingEnrollmentKey();
        persist(state, existing);

        UpdateBillingStatusRequest request = new UpdateBillingStatusRequest();
        request.setStatus(BillingStatus.PAID);

        BillingResponse response = billingService.updateStatus(existing.getId(), request);

        assertThat(response.status()).isEqualTo(BillingStatus.PAID);
        assertThat(state.storage.get(existing.getId()).getPendingEnrollmentKey()).isNull();
    }

    private static BillingRepository repository(RepositoryState state) {
        InvocationHandler handler = new BillingRepositoryHandler(state);
        return (BillingRepository) Proxy.newProxyInstance(
                BillingRepository.class.getClassLoader(),
                new Class<?>[] {BillingRepository.class},
                handler);
    }

    private static Billing persist(RepositoryState state, Billing billing) {
        if (billing.getId() == null) {
            billing.setId(state.sequence++);
        }
        state.storage.put(billing.getId(), billing);
        return billing;
    }

    private static final class RepositoryState {
        private final Map<Long, Billing> storage = new HashMap<>();
        private long sequence = 1L;
    }

    private static final class BillingRepositoryHandler implements InvocationHandler {

        private final RepositoryState state;

        private BillingRepositoryHandler(RepositoryState state) {
            this.state = state;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            return switch (method.getName()) {
                case "existsByPendingEnrollmentKey" -> state.storage.values().stream()
                        .anyMatch(billing -> billing.getPendingEnrollmentKey() != null
                                && billing.getPendingEnrollmentKey().equals(args[0]));
                case "existsByPendingEnrollmentKeyAndIdNot" -> state.storage.values().stream()
                        .anyMatch(billing -> billing.getPendingEnrollmentKey() != null
                                && billing.getPendingEnrollmentKey().equals(args[0])
                                && !billing.getId().equals(args[1]));
                case "findAllByOrderByCreatedAtDescIdDesc" -> state.storage.values().stream()
                        .sorted(Comparator.comparing(Billing::getCreatedAt)
                                .reversed()
                                .thenComparing(Billing::getId, Comparator.reverseOrder()))
                        .toList();
                case "findById" -> Optional.ofNullable(state.storage.get(args[0]));
                case "save" -> persist(state, (Billing) args[0]);
                case "toString" -> "BillingRepositoryTestProxy";
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> proxy == args[0];
                default -> throw new UnsupportedOperationException("Unsupported repository method: " + method.getName());
            };
        }
    }
}
