package com.campusenroll.enrollmentservice.enrollment;

import com.campusenroll.enrollmentservice.enrollment.dto.CreateEnrollmentRequest;
import com.campusenroll.enrollmentservice.enrollment.dto.EnrollmentResponse;
import com.campusenroll.enrollmentservice.enrollment.dto.UpdateEnrollmentStatusRequest;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @GetMapping
    public ResponseEntity<List<EnrollmentResponse>> getAll() {
        return ResponseEntity.ok(enrollmentService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EnrollmentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(enrollmentService.getById(id));
    }

    @PostMapping
    public ResponseEntity<EnrollmentResponse> create(@Valid @RequestBody CreateEnrollmentRequest request) {
        EnrollmentResponse response = enrollmentService.create(request);
        return ResponseEntity.created(URI.create("/api/enrollments/" + response.id())).body(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<EnrollmentResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEnrollmentStatusRequest request) {
        return ResponseEntity.ok(enrollmentService.updateStatus(id, request));
    }
}
