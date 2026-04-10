package com.campusenroll.studentservice.student.error;

import java.util.Map;

public record ApiErrorResponse(
        String message,
        Map<String, String> errors) {
}
