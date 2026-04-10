package com.campusenroll.courseservice.catalog.repository;

import com.campusenroll.courseservice.catalog.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {

    boolean existsByCourseCodeIgnoreCase(String courseCode);
}
