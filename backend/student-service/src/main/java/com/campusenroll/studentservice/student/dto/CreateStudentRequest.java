package com.campusenroll.studentservice.student.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateStudentRequest(
        @NotBlank(message = "studentCode is required")
        String studentCode,
        @NotBlank(message = "firstName is required")
        String firstName,
        @NotBlank(message = "lastName is required")
        String lastName,
        @Email(message = "email must be a valid email address")
        String email,
        @NotNull(message = "active must be provided")
        Boolean active) {
}
