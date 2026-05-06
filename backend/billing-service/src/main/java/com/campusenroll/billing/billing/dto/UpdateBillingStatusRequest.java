package com.campusenroll.billing.billing.dto;

import com.campusenroll.billing.billing.BillingStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateBillingStatusRequest {

    @NotNull(message = "status is required")
    private BillingStatus status;

    public BillingStatus getStatus() {
        return status;
    }

    public void setStatus(BillingStatus status) {
        this.status = status;
    }
}
