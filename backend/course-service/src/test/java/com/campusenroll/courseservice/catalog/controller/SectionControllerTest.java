package com.campusenroll.courseservice.catalog.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.campusenroll.courseservice.catalog.exception.ApiExceptionHandler;
import com.campusenroll.courseservice.catalog.model.AcademicPeriod;
import com.campusenroll.courseservice.catalog.model.Course;
import com.campusenroll.courseservice.catalog.model.ScheduleBlock;
import com.campusenroll.courseservice.catalog.model.Section;
import com.campusenroll.courseservice.catalog.repository.AcademicPeriodRepository;
import com.campusenroll.courseservice.catalog.repository.CourseRepository;
import com.campusenroll.courseservice.catalog.repository.SectionRepository;
import com.campusenroll.courseservice.catalog.service.AcademicPeriodService;
import com.campusenroll.courseservice.catalog.service.CourseService;
import com.campusenroll.courseservice.catalog.service.SectionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

class SectionControllerTest {

    private final SectionRepository sectionRepository = mock(SectionRepository.class);
    private final CourseRepository courseRepository = mock(CourseRepository.class);
    private final AcademicPeriodRepository academicPeriodRepository = mock(AcademicPeriodRepository.class);
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        CourseService courseService = new CourseService(courseRepository);
        AcademicPeriodService academicPeriodService = new AcademicPeriodService(academicPeriodRepository);
        SectionService sectionService = new SectionService(sectionRepository, courseService, academicPeriodService);
        SectionController controller = new SectionController(sectionService);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ApiExceptionHandler())
                .setValidator(validator)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void shouldListSections() throws Exception {
        Course course = new Course();
        course.setId(1L);
        course.setCourseCode("CS101");
        course.setName("Intro to Programming");
        course.setCredits(4);
        course.setActive(true);

        AcademicPeriod period = new AcademicPeriod();
        period.setId(2L);
        period.setName("2026 Semester 1");
        period.setActive(true);

        ScheduleBlock block = new ScheduleBlock();
        block.setDayOfWeek(DayOfWeek.MONDAY);
        block.setStartTime(LocalTime.of(8, 0));
        block.setEndTime(LocalTime.of(9, 30));

        Section section = new Section();
        section.setId(1L);
        section.setSectionCode("CS101-A");
        section.setCapacity(30);
        section.setActive(true);
        section.setCourse(course);
        section.setAcademicPeriod(period);
        section.setScheduleBlocks(List.of(block));

        when(sectionRepository.findAll(any(org.springframework.data.domain.Sort.class))).thenReturn(List.of(section));

        mockMvc.perform(get("/api/sections"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sectionCode").value("CS101-A"))
                .andExpect(jsonPath("$[0].scheduleBlocks[0].dayOfWeek").value("MONDAY"));
    }

    @Test
    void shouldCreateSection() throws Exception {
        Course course = new Course();
        course.setId(1L);
        course.setCourseCode("CS101");
        course.setName("Intro to Programming");
        course.setCredits(4);
        course.setActive(true);

        AcademicPeriod period = new AcademicPeriod();
        period.setId(2L);
        period.setName("2026 Semester 1");
        period.setActive(true);

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(academicPeriodRepository.findById(2L)).thenReturn(Optional.of(period));
        when(sectionRepository.save(any(Section.class))).thenAnswer(invocation -> {
            Section section = invocation.getArgument(0);
            section.setId(1L);
            return section;
        });

        mockMvc.perform(post("/api/sections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateSectionPayload(
                                "CS101-A",
                                30,
                                true,
                                1L,
                                2L,
                                List.of(new CreateScheduleBlockPayload("MONDAY", "08:00:00", "09:30:00"))))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.courseId").value(1L))
                .andExpect(jsonPath("$.academicPeriodId").value(2L));
    }

    @Test
    void shouldRejectSectionWithInvalidScheduleRange() throws Exception {
        mockMvc.perform(post("/api/sections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateSectionPayload(
                                "CS101-A",
                                30,
                                true,
                                1L,
                                2L,
                                List.of(new CreateScheduleBlockPayload("MONDAY", "10:00:00", "09:30:00"))))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("endTime must be after startTime"));
    }

    private record CreateSectionPayload(
            String sectionCode,
            Integer capacity,
            Boolean active,
            Long courseId,
            Long academicPeriodId,
            List<CreateScheduleBlockPayload> scheduleBlocks) {
    }

    private record CreateScheduleBlockPayload(String dayOfWeek, String startTime, String endTime) {
    }
}
