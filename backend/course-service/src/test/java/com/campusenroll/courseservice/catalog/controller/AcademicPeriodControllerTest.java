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
import com.campusenroll.courseservice.catalog.repository.AcademicPeriodRepository;
import com.campusenroll.courseservice.catalog.service.AcademicPeriodService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

class AcademicPeriodControllerTest {

    private final AcademicPeriodRepository academicPeriodRepository = mock(AcademicPeriodRepository.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        AcademicPeriodService academicPeriodService = new AcademicPeriodService(academicPeriodRepository);
        AcademicPeriodController controller = new AcademicPeriodController(academicPeriodService);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ApiExceptionHandler())
                .setValidator(validator)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void shouldListAcademicPeriods() throws Exception {
        AcademicPeriod period = new AcademicPeriod();
        period.setId(1L);
        period.setName("2026 Semester 1");
        period.setActive(true);

        when(academicPeriodRepository.findAll(any(org.springframework.data.domain.Sort.class))).thenReturn(List.of(period));

        mockMvc.perform(get("/api/periods"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("2026 Semester 1"));
    }

    @Test
    void shouldCreateAcademicPeriod() throws Exception {
        when(academicPeriodRepository.save(any(AcademicPeriod.class))).thenAnswer(invocation -> {
            AcademicPeriod period = invocation.getArgument(0);
            period.setId(1L);
            return period;
        });

        mockMvc.perform(post("/api/periods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreatePeriodPayload("2026 Semester 1", true))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    private record CreatePeriodPayload(String name, Boolean active) {
    }
}
