package com.campusenroll.studentservice.student.dto;

import jakarta.validation.constraints.NotNull;

public record StudentStatusUpdateRequest(
        @NotNull(message = "active must be provided")
        Boolean active) {
}
