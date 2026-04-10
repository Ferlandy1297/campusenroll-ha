package com.campusenroll.courseservice.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AcademicPeriodRequest(
        @NotBlank(message = "name is required")
        String name,
        @NotNull(message = "active is required")
        Boolean active) {
}
