package com.airouteviva.service;

import com.airouteviva.dto.request.CreateStudentRequest;
import com.airouteviva.dto.response.StudentResponse;
import com.airouteviva.entity.Student;
import com.airouteviva.exception.DuplicateResourceException;
import com.airouteviva.exception.ResourceNotFoundException;
import com.airouteviva.repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StudentService {

    private static final Logger log = LoggerFactory.getLogger(StudentService.class);

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Transactional
    public StudentResponse createStudent(CreateStudentRequest request) {
        if (studentRepository.existsByStudentId(request.getStudentId())) {
            throw new DuplicateResourceException("Student with ID " + request.getStudentId() + " already exists");
        }

        Student student = new Student(request.getStudentId(), request.getName());
        student = studentRepository.save(student);
        log.info("Created student: id={}, name={}", student.getStudentId(), student.getName());

        return mapToResponse(student);
    }

    @Transactional(readOnly = true)
    public StudentResponse getStudent(String studentId) {
        Student student = studentRepository.findByStudentId(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + studentId));
        return mapToResponse(student);
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> getAllStudents() {
        return studentRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    private StudentResponse mapToResponse(Student student) {
        return new StudentResponse(student.getStudentId(), student.getName(), student.getCreatedAt());
    }
}
