package com.campusenroll.studentservice.student;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.campusenroll.studentservice.student.dto.CreateStudentRequest;
import com.campusenroll.studentservice.student.dto.StudentResponse;
import com.campusenroll.studentservice.student.dto.StudentStatusUpdateRequest;
import com.campusenroll.studentservice.student.error.DuplicateStudentCodeException;
import com.campusenroll.studentservice.student.error.StudentNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private StudentService studentService;

    @Test
    void shouldCreateStudentWhenStudentCodeIsUnique() {
        given(studentRepository.existsByStudentCode("STU-001")).willReturn(false);
        given(studentRepository.save(any(Student.class))).willAnswer(invocation -> {
            Student student = invocation.getArgument(0);
            TestStudentFactory.assignId(student, 1L);
            return student;
        });

        StudentResponse response = studentService.createStudent(
                new CreateStudentRequest(" STU-001 ", " Ana ", " Lopez ", " ana@example.com ", true));

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.studentCode()).isEqualTo("STU-001");
        assertThat(response.firstName()).isEqualTo("Ana");
        assertThat(response.lastName()).isEqualTo("Lopez");
        assertThat(response.email()).isEqualTo("ana@example.com");
        assertThat(response.active()).isTrue();
    }

    @Test
    void shouldRejectDuplicateStudentCode() {
        given(studentRepository.existsByStudentCode("STU-001")).willReturn(true);

        assertThatThrownBy(() -> studentService.createStudent(
                new CreateStudentRequest("STU-001", "Ana", "Lopez", null, true)))
                .isInstanceOf(DuplicateStudentCodeException.class)
                .hasMessage("Student with code 'STU-001' already exists");
    }

    @Test
    void shouldReturnStudentById() {
        Student student = TestStudentFactory.student(1L, "STU-001", "Ana", "Lopez", "ana@example.com", true);
        given(studentRepository.findById(1L)).willReturn(Optional.of(student));

        StudentResponse response = studentService.getStudentById(1L);

        assertThat(response.studentCode()).isEqualTo("STU-001");
        assertThat(response.email()).isEqualTo("ana@example.com");
    }

    @Test
    void shouldThrowWhenStudentIsMissing() {
        given(studentRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.getStudentById(99L))
                .isInstanceOf(StudentNotFoundException.class)
                .hasMessage("Student with id '99' was not found");
    }

    @Test
    void shouldUpdateStudentStatus() {
        Student student = TestStudentFactory.student(1L, "STU-001", "Ana", "Lopez", "ana@example.com", true);
        given(studentRepository.findById(1L)).willReturn(Optional.of(student));
        given(studentRepository.save(any(Student.class))).willAnswer(invocation -> invocation.getArgument(0));

        StudentResponse response = studentService.updateStudentStatus(1L, new StudentStatusUpdateRequest(false));

        assertThat(response.active()).isFalse();
        verify(studentRepository).save(student);
    }

    @Test
    void shouldPersistNormalizedValues() {
        given(studentRepository.existsByStudentCode("STU-009")).willReturn(false);
        given(studentRepository.save(any(Student.class))).willAnswer(invocation -> invocation.getArgument(0));

        studentService.createStudent(new CreateStudentRequest(" STU-009 ", " Ana ", " Lopez ", " ", true));

        ArgumentCaptor<Student> captor = ArgumentCaptor.forClass(Student.class);
        verify(studentRepository).save(captor.capture());
        Student savedStudent = captor.getValue();
        assertThat(savedStudent.getStudentCode()).isEqualTo("STU-009");
        assertThat(savedStudent.getFirstName()).isEqualTo("Ana");
        assertThat(savedStudent.getLastName()).isEqualTo("Lopez");
        assertThat(savedStudent.getEmail()).isNull();
    }
}
