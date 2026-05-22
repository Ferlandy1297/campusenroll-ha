package com.campusenroll.billing.billing;

import com.campusenroll.billing.billing.dto.BillingResponse;
import com.campusenroll.billing.billing.dto.CreateBillingRequest;
import com.campusenroll.billing.billing.dto.UpdateBillingStatusRequest;
import com.campusenroll.billing.idempotency.IdempotentResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/billings")
public class BillingController {

    private final BillingService billingService;

    public BillingController(BillingService billingService) {
        this.billingService = billingService;
    }

    @GetMapping
    public ResponseEntity<List<BillingResponse>> getAll() {
        return ResponseEntity.ok(billingService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BillingResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(billingService.getById(id));
    }

    @PostMapping
    public ResponseEntity<BillingResponse> create(
            @Valid @RequestBody CreateBillingRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        if (!StringUtils.hasText(idempotencyKey)) {
            BillingResponse response = billingService.create(request);
            return ResponseEntity.created(URI.create("/api/billings/" + response.id())).body(response);
        }

        IdempotentResponse<BillingResponse> response = billingService.create(request, idempotencyKey);
        return ResponseEntity.status(response.status())
                .location(URI.create("/api/billings/" + response.body().id()))
                .body(response.body());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<BillingResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBillingStatusRequest request) {
        return ResponseEntity.ok(billingService.updateStatus(id, request));
    }
}
