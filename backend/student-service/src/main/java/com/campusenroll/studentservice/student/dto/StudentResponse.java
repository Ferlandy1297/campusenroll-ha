package com.campusenroll.studentservice.student.dto;

import com.campusenroll.studentservice.student.Student;

public record StudentResponse(
        Long id,
        String studentCode,
        String firstName,
        String lastName,
        String email,
        boolean active) {

    public static StudentResponse from(Student student) {
        return new StudentResponse(
                student.getId(),
                student.getStudentCode(),
                student.getFirstName(),
                student.getLastName(),
                student.getEmail(),
                student.isActive());
    }
}
