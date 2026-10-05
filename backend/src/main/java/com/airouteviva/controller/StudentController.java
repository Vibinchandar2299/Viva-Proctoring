package com.airouteviva.controller;

import com.airouteviva.dto.request.CreateStudentRequest;
import com.airouteviva.dto.response.StudentResponse;
import com.airouteviva.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@Tag(name = "Student Management", description = "Candidate registration and retrieval APIs")
@CrossOrigin(origins = "*")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping
    @Operation(summary = "Register candidate student", description = "Creates a new student record for viva proctoring")
    public ResponseEntity<StudentResponse> createStudent(@Valid @RequestBody CreateStudentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(studentService.createStudent(request));
    }

    @GetMapping("/{studentId}")
    @Operation(summary = "Get student details", description = "Retrieves student record by studentId")
    public ResponseEntity<StudentResponse> getStudent(@PathVariable String studentId) {
        return ResponseEntity.ok(studentService.getStudent(studentId));
    }

    @GetMapping
    @Operation(summary = "List all students", description = "Returns all registered students")
    public ResponseEntity<List<StudentResponse>> getAllStudents() {
        return ResponseEntity.ok(studentService.getAllStudents());
    }
}
