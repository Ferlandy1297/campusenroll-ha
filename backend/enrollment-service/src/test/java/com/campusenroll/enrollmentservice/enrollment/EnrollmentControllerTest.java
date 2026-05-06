package com.campusenroll.enrollmentservice.enrollment;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.campusenroll.enrollmentservice.enrollment.dto.EnrollmentResponse;
import com.campusenroll.enrollmentservice.error.ConflictException;
import com.campusenroll.enrollmentservice.error.GlobalExceptionHandler;
import com.campusenroll.enrollmentservice.error.ResourceNotFoundException;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class EnrollmentControllerTest {

    private MockMvc mockMvc;
    private StubEnrollmentService enrollmentService;

    @BeforeEach
    void setUp() {
        enrollmentService = new StubEnrollmentService();
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(new EnrollmentController(enrollmentService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void shouldListEnrollments() throws Exception {
        enrollmentService.getAllResponse = List.of(
                new EnrollmentResponse(1L, 100L, 200L, EnrollmentStatus.ENROLLED, OffsetDateTime.parse("2026-05-05T10:15:30Z")));

        mockMvc.perform(get("/api/enrollments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("ENROLLED"));
    }

    @Test
    void shouldCreateEnrollment() throws Exception {
        enrollmentService.createResponse =
                new EnrollmentResponse(1L, 100L, 200L, EnrollmentStatus.ENROLLED, OffsetDateTime.parse("2026-05-05T10:15:30Z"));

        mockMvc.perform(post("/api/enrollments")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "studentId": 100,
                                  "sectionId": 200
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/enrollments/1"))
                .andExpect(jsonPath("$.studentId").value(100))
                .andExpect(jsonPath("$.sectionId").value(200))
                .andExpect(jsonPath("$.status").value("ENROLLED"));
    }

    @Test
    void shouldRejectCreateWhenRequiredFieldsAreMissing() throws Exception {
        mockMvc.perform(post("/api/enrollments")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details[0]").exists())
                .andExpect(jsonPath("$.details[1]").exists());
    }

    @Test
    void shouldReturnNotFoundForMissingEnrollment() throws Exception {
        enrollmentService.getByIdException = new ResourceNotFoundException("Enrollment not found");

        mockMvc.perform(get("/api/enrollments/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Enrollment not found"));
    }

    @Test
    void shouldRejectInvalidStatusPayload() throws Exception {
        mockMvc.perform(patch("/api/enrollments/1/status")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "INVALID"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid request payload"))
                .andExpect(jsonPath("$.details[0]").value("status must be valid"));
    }

    @Test
    void shouldReturnConflictForDuplicateEnrollment() throws Exception {
        enrollmentService.createException = new ConflictException("An active enrollment already exists for this student and section");

        mockMvc.perform(post("/api/enrollments")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "studentId": 100,
                                  "sectionId": 200
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("An active enrollment already exists for this student and section"));
    }

    private static final class StubEnrollmentService extends EnrollmentService {

        private List<EnrollmentResponse> getAllResponse = List.of();
        private EnrollmentResponse createResponse;
        private RuntimeException createException;
        private RuntimeException getByIdException;

        private StubEnrollmentService() {
            super(null);
        }

        @Override
        public List<EnrollmentResponse> getAll() {
            return getAllResponse;
        }

        @Override
        public EnrollmentResponse getById(Long id) {
            if (getByIdException != null) {
                throw getByIdException;
            }
            return createResponse;
        }

        @Override
        public EnrollmentResponse create(com.campusenroll.enrollmentservice.enrollment.dto.CreateEnrollmentRequest request) {
            if (createException != null) {
                throw createException;
            }
            return createResponse;
        }
    }
}
