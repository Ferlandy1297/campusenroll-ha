package com.campusenroll.studentservice.student;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.campusenroll.studentservice.student.dto.CreateStudentRequest;
import com.campusenroll.studentservice.student.dto.StudentResponse;
import com.campusenroll.studentservice.student.dto.StudentStatusUpdateRequest;
import com.campusenroll.studentservice.student.error.DuplicateStudentCodeException;
import com.campusenroll.studentservice.student.error.GlobalExceptionHandler;
import com.campusenroll.studentservice.student.error.StudentNotFoundException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class StudentControllerTest {

    private final StubStudentService studentService = new StubStudentService();

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new StudentController(studentService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldReturnAllStudents() throws Exception {
        studentService.students = List.of(new StudentResponse(1L, "STU-001", "Ana", "Lopez", "ana@example.com", true));

        mockMvc.perform(get("/api/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].studentCode").value("STU-001"))
                .andExpect(jsonPath("$[0].active").value(true));
    }

    @Test
    void shouldReturnStudentById() throws Exception {
        studentService.studentById = new StudentResponse(1L, "STU-001", "Ana", "Lopez", "ana@example.com", true);

        mockMvc.perform(get("/api/students/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.studentCode").value("STU-001"));
    }

    @Test
    void shouldCreateStudent() throws Exception {
        studentService.createdStudent = new StudentResponse(1L, "STU-001", "Ana", "Lopez", "ana@example.com", true);

        mockMvc.perform(post("/api/students")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "studentCode": "STU-001",
                                  "firstName": "Ana",
                                  "lastName": "Lopez",
                                  "email": "ana@example.com",
                                  "active": true
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/students/1"))
                .andExpect(jsonPath("$.studentCode").value("STU-001"));
    }

    @Test
    void shouldRejectInvalidCreateStudentRequest() throws Exception {
        mockMvc.perform(post("/api/students")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "studentCode": "",
                                  "firstName": "",
                                  "lastName": " ",
                                  "active": null
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.studentCode").value("studentCode is required"))
                .andExpect(jsonPath("$.errors.firstName").value("firstName is required"))
                .andExpect(jsonPath("$.errors.lastName").value("lastName is required"))
                .andExpect(jsonPath("$.errors.active").value("active must be provided"));
    }

    @Test
    void shouldReturnConflictForDuplicateStudentCode() throws Exception {
        studentService.createException = new DuplicateStudentCodeException("STU-001");

        mockMvc.perform(post("/api/students")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "studentCode": "STU-001",
                                  "firstName": "Ana",
                                  "lastName": "Lopez",
                                  "active": true
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Student with code 'STU-001' already exists"))
                .andExpect(jsonPath("$.errors.studentCode").value("studentCode must be unique"));
    }

    @Test
    void shouldUpdateStudentStatus() throws Exception {
        studentService.updatedStudent = new StudentResponse(1L, "STU-001", "Ana", "Lopez", "ana@example.com", false);

        mockMvc.perform(patch("/api/students/1/status")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "active": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void shouldReturnNotFoundWhenStudentDoesNotExist() throws Exception {
        studentService.studentByIdException = new StudentNotFoundException(99L);

        mockMvc.perform(get("/api/students/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Student with id '99' was not found"));
    }

    private static class StubStudentService extends StudentService {

        private List<StudentResponse> students = List.of();
        private StudentResponse studentById;
        private StudentResponse createdStudent;
        private StudentResponse updatedStudent;
        private RuntimeException createException;
        private RuntimeException studentByIdException;

        StubStudentService() {
            super(null);
        }

        @Override
        public List<StudentResponse> getAllStudents() {
            return students;
        }

        @Override
        public StudentResponse getStudentById(Long id) {
            if (studentByIdException != null) {
                throw studentByIdException;
            }
            return studentById;
        }

        @Override
        public StudentResponse createStudent(CreateStudentRequest request) {
            if (createException != null) {
                throw createException;
            }
            return createdStudent;
        }

        @Override
        public StudentResponse updateStudentStatus(Long id, StudentStatusUpdateRequest request) {
            return updatedStudent;
        }
    }
}
