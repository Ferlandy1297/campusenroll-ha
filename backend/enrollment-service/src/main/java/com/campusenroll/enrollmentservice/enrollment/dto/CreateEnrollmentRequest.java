package com.campusenroll.enrollmentservice.enrollment.dto;

import jakarta.validation.constraints.NotNull;

public class CreateEnrollmentRequest {

    @NotNull(message = "studentId is required")
    private Long studentId;

    @NotNull(message = "sectionId is required")
    private Long sectionId;

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getSectionId() {
        return sectionId;
    }

    public void setSectionId(Long sectionId) {
        this.sectionId = sectionId;
    }
}
