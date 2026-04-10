package com.campusenroll.courseservice.catalog.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CourseRequest(
        @NotBlank(message = "courseCode is required")
        String courseCode,
        @NotBlank(message = "name is required")
        String name,
        @NotNull(message = "credits is required")
        @Min(value = 0, message = "credits must be greater than or equal to zero")
        Integer credits,
        @NotNull(message = "active is required")
        Boolean active) {
}
