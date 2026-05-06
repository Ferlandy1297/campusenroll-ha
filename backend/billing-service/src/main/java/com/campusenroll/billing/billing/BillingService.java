package com.campusenroll.billing.billing;

import com.campusenroll.billing.billing.dto.BillingResponse;
import com.campusenroll.billing.billing.dto.CreateBillingRequest;
import com.campusenroll.billing.billing.dto.UpdateBillingStatusRequest;
import com.campusenroll.billing.error.ConflictException;
import com.campusenroll.billing.error.ResourceNotFoundException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BillingService {

    private final BillingRepository billingRepository;

    public BillingService(BillingRepository billingRepository) {
        this.billingRepository = billingRepository;
    }

    @Transactional(readOnly = true)
    public List<BillingResponse> getAll() {
        return billingRepository.findAllByOrderByCreatedAtDescIdDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BillingResponse getById(Long id) {
        return toResponse(findBilling(id));
    }

    @Transactional
    public BillingResponse create(CreateBillingRequest request) {
        if (request.getStatus() == BillingStatus.PENDING
                && billingRepository.existsByPendingEnrollmentKey(request.getEnrollmentId())) {
            throw new ConflictException("An active billing already exists for this enrollment");
        }

        Billing billing = new Billing();
        billing.setEnrollmentId(request.getEnrollmentId());
        billing.setAmount(request.getAmount());
        billing.setCurrency(normalizeCurrency(request.getCurrency()));
        billing.setStatus(request.getStatus());
        billing.setCreatedAt(OffsetDateTime.now());
        billing.syncPendingEnrollmentKey();

        return saveBilling(billing);
    }

    @Transactional
    public BillingResponse updateStatus(Long id, UpdateBillingStatusRequest request) {
        Billing billing = findBilling(id);
        BillingStatus nextStatus = request.getStatus();

        if (nextStatus == BillingStatus.PENDING
                && billing.getStatus() != BillingStatus.PENDING
                && billingRepository.existsByPendingEnrollmentKeyAndIdNot(billing.getEnrollmentId(), billing.getId())) {
            throw new ConflictException("An active billing already exists for this enrollment");
        }

        billing.setStatus(nextStatus);
        billing.syncPendingEnrollmentKey();
        return saveBilling(billing);
    }

    private BillingResponse saveBilling(Billing billing) {
        try {
            return toResponse(billingRepository.save(billing));
        } catch (DataIntegrityViolationException ex) {
            if (billing.getStatus() == BillingStatus.PENDING) {
                throw new ConflictException("An active billing already exists for this enrollment");
            }
            throw new ConflictException("A billing with this enrollment and status already exists");
        }
    }

    private Billing findBilling(Long id) {
        return billingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Billing not found"));
    }

    private BillingResponse toResponse(Billing billing) {
        return new BillingResponse(
                billing.getId(),
                billing.getEnrollmentId(),
                billing.getAmount(),
                billing.getCurrency(),
                billing.getStatus(),
                billing.getCreatedAt());
    }

    private String normalizeCurrency(String currency) {
        return currency.trim().toUpperCase(Locale.ROOT);
    }
}
