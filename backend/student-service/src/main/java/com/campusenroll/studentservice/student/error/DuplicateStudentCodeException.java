package com.campusenroll.studentservice.student.error;

public class DuplicateStudentCodeException extends RuntimeException {

    public DuplicateStudentCodeException(String studentCode) {
        super("Student with code '%s' already exists".formatted(studentCode));
    }
}
