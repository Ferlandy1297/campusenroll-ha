package com.campusenroll.studentservice.student;

import com.campusenroll.studentservice.student.dto.CreateStudentRequest;
import com.campusenroll.studentservice.student.dto.StudentResponse;
import com.campusenroll.studentservice.student.dto.StudentStatusUpdateRequest;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    public ResponseEntity<List<StudentResponse>> getStudents() {
        return ResponseEntity.ok(studentService.getAllStudents());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentResponse> getStudentById(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.getStudentById(id));
    }

    @PostMapping
    public ResponseEntity<StudentResponse> createStudent(@Valid @RequestBody CreateStudentRequest request) {
        StudentResponse createdStudent = studentService.createStudent(request);
        return ResponseEntity
                .created(URI.create("/api/students/" + createdStudent.id()))
                .body(createdStudent);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<StudentResponse> updateStudentStatus(
            @PathVariable Long id,
            @Valid @RequestBody StudentStatusUpdateRequest request) {
        return ResponseEntity.ok(studentService.updateStudentStatus(id, request));
    }
}
