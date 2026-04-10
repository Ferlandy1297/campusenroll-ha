package com.campusenroll.studentservice.student;

import com.campusenroll.studentservice.student.dto.CreateStudentRequest;
import com.campusenroll.studentservice.student.dto.StudentResponse;
import com.campusenroll.studentservice.student.dto.StudentStatusUpdateRequest;
import com.campusenroll.studentservice.student.error.DuplicateStudentCodeException;
import com.campusenroll.studentservice.student.error.StudentNotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public List<StudentResponse> getAllStudents() {
        return studentRepository.findAll().stream()
                .map(StudentResponse::from)
                .toList();
    }

    public StudentResponse getStudentById(Long id) {
        return StudentResponse.from(findStudent(id));
    }

    @Transactional
    public StudentResponse createStudent(CreateStudentRequest request) {
        if (studentRepository.existsByStudentCode(request.studentCode().trim())) {
            throw new DuplicateStudentCodeException(request.studentCode().trim());
        }

        Student student = new Student();
        student.setStudentCode(request.studentCode().trim());
        student.setFirstName(request.firstName().trim());
        student.setLastName(request.lastName().trim());
        student.setEmail(normalizeEmail(request.email()));
        student.setActive(request.active());

        return StudentResponse.from(studentRepository.save(student));
    }

    @Transactional
    public StudentResponse updateStudentStatus(Long id, StudentStatusUpdateRequest request) {
        Student student = findStudent(id);
        student.setActive(request.active());
        return StudentResponse.from(studentRepository.save(student));
    }

    private Student findStudent(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new StudentNotFoundException(id));
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }

        String trimmedEmail = email.trim();
        return trimmedEmail.isEmpty() ? null : trimmedEmail;
    }
}
