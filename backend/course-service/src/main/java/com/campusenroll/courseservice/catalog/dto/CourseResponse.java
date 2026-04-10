package com.campusenroll.courseservice.catalog.dto;

public record CourseResponse(
        Long id,
        String courseCode,
        String name,
        Integer credits,
        Boolean active) {
}
