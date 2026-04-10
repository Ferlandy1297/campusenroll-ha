package com.campusenroll.courseservice.catalog.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record SectionRequest(
        @NotBlank(message = "sectionCode is required")
        String sectionCode,
        @NotNull(message = "capacity is required")
        @Min(value = 1, message = "capacity must be greater than zero")
        Integer capacity,
        @NotNull(message = "active is required")
        Boolean active,
        @NotNull(message = "courseId is required")
        Long courseId,
        @NotNull(message = "academicPeriodId is required")
        Long academicPeriodId,
        @NotEmpty(message = "scheduleBlocks is required")
        List<@Valid ScheduleBlockRequest> scheduleBlocks) {
}
