package com.campusenroll.studentservice.student.error;

public class StudentNotFoundException extends RuntimeException {

    public StudentNotFoundException(Long id) {
        super("Student with id '%d' was not found".formatted(id));
    }
}
