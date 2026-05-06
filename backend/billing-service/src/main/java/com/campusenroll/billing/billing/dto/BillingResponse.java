package com.campusenroll.billing.billing.dto;

import com.campusenroll.billing.billing.BillingStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record BillingResponse(
        Long id,
        Long enrollmentId,
        BigDecimal amount,
        String currency,
        BillingStatus status,
        OffsetDateTime createdAt) {
}
