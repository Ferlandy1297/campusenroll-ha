package com.campusenroll.courseservice.catalog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.campusenroll.courseservice.catalog.dto.CourseRequest;
import com.campusenroll.courseservice.catalog.exception.ResourceConflictException;
import com.campusenroll.courseservice.catalog.exception.ResourceNotFoundException;
import com.campusenroll.courseservice.catalog.model.Course;
import com.campusenroll.courseservice.catalog.repository.CourseRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private CourseService courseService;

    @Test
    void shouldRejectDuplicateCourseCode() {
        when(courseRepository.existsByCourseCodeIgnoreCase("CS101")).thenReturn(true);

        assertThatThrownBy(() -> courseService.createCourse(new CourseRequest(" CS101 ", "Intro", 4, true)))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessage("courseCode already exists");
    }

    @Test
    void shouldTrimAndSaveCourse() {
        when(courseRepository.existsByCourseCodeIgnoreCase("CS101")).thenReturn(false);
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> {
            Course course = invocation.getArgument(0);
            course.setId(10L);
            return course;
        });

        var response = courseService.createCourse(new CourseRequest(" CS101 ", " Intro ", 4, true));

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.courseCode()).isEqualTo("CS101");
        assertThat(response.name()).isEqualTo("Intro");
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    void shouldThrowWhenCourseDoesNotExist() {
        when(courseRepository.findById(77L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.getCourse(77L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Course not found for id 77");
    }
}
