package com.campusenroll.studentservice.student;

import java.lang.reflect.Field;

final class TestStudentFactory {

    private TestStudentFactory() {
    }

    static Student student(Long id, String studentCode, String firstName, String lastName, String email, boolean active) {
        Student student = new Student();
        assignId(student, id);
        student.setStudentCode(studentCode);
        student.setFirstName(firstName);
        student.setLastName(lastName);
        student.setEmail(email);
        student.setActive(active);
        return student;
    }

    static void assignId(Student student, Long id) {
        try {
            Field field = Student.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(student, id);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to assign test student id", exception);
        }
    }
}
