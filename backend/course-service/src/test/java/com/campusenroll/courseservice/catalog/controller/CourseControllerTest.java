package com.campusenroll.courseservice.catalog.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.campusenroll.courseservice.catalog.exception.ApiExceptionHandler;
import com.campusenroll.courseservice.catalog.model.Course;
import com.campusenroll.courseservice.catalog.repository.CourseRepository;
import com.campusenroll.courseservice.catalog.service.CourseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

class CourseControllerTest {

    private final CourseRepository courseRepository = mock(CourseRepository.class);
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        CourseService courseService = new CourseService(courseRepository);
        CourseController controller = new CourseController(courseService);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ApiExceptionHandler())
                .setValidator(validator)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void shouldListCourses() throws Exception {
        Course course = new Course();
        course.setId(1L);
        course.setCourseCode("CS101");
        course.setName("Intro to Programming");
        course.setCredits(4);
        course.setActive(true);

        when(courseRepository.findAll(any(org.springframework.data.domain.Sort.class))).thenReturn(List.of(course));

        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].courseCode").value("CS101"));
    }

    @Test
    void shouldGetCourseById() throws Exception {
        Course course = new Course();
        course.setId(1L);
        course.setCourseCode("CS101");
        course.setName("Intro to Programming");
        course.setCredits(4);
        course.setActive(true);

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        mockMvc.perform(get("/api/courses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Intro to Programming"));
    }

    @Test
    void shouldCreateCourse() throws Exception {
        when(courseRepository.existsByCourseCodeIgnoreCase("CS101")).thenReturn(false);
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> {
            Course course = invocation.getArgument(0);
            course.setId(1L);
            return course;
        });

        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateCoursePayload("CS101", "Intro to Programming", 4, true))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void shouldRejectInvalidCourseRequest() throws Exception {
        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateCoursePayload("", "", -1, null))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.courseCode").value("courseCode is required"))
                .andExpect(jsonPath("$.fieldErrors.name").value("name is required"));
    }

    @Test
    void shouldReturnConflictWhenCourseCodeAlreadyExists() throws Exception {
        when(courseRepository.existsByCourseCodeIgnoreCase("CS101")).thenReturn(true);

        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateCoursePayload("CS101", "Intro to Programming", 4, true))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("courseCode already exists"));
    }

    private record CreateCoursePayload(String courseCode, String name, Integer credits, Boolean active) {
    }
}
