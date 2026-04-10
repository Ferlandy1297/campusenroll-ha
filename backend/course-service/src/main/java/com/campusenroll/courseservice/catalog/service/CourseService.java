package com.campusenroll.courseservice.catalog.service;

import com.campusenroll.courseservice.catalog.dto.CourseRequest;
import com.campusenroll.courseservice.catalog.dto.CourseResponse;
import com.campusenroll.courseservice.catalog.exception.ResourceConflictException;
import com.campusenroll.courseservice.catalog.exception.ResourceNotFoundException;
import com.campusenroll.courseservice.catalog.model.Course;
import com.campusenroll.courseservice.catalog.repository.CourseRepository;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CourseService {

    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> getCourses() {
        return courseRepository.findAll(Sort.by("courseCode").ascending()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CourseResponse getCourse(Long id) {
        return toResponse(findCourse(id));
    }

    @Transactional
    public CourseResponse createCourse(CourseRequest request) {
        String normalizedCourseCode = normalize(request.courseCode());
        if (courseRepository.existsByCourseCodeIgnoreCase(normalizedCourseCode)) {
            throw new ResourceConflictException("courseCode already exists");
        }

        Course course = new Course();
        course.setCourseCode(normalizedCourseCode);
        course.setName(normalize(request.name()));
        course.setCredits(request.credits());
        course.setActive(request.active());

        return toResponse(courseRepository.save(course));
    }

    @Transactional(readOnly = true)
    public Course findCourse(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found for id " + id));
    }

    private CourseResponse toResponse(Course course) {
        return new CourseResponse(
                course.getId(),
                course.getCourseCode(),
                course.getName(),
                course.getCredits(),
                course.getActive());
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }
}
