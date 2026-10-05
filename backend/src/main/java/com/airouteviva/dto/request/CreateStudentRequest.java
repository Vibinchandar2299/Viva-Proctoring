package com.airouteviva.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateStudentRequest {

    @NotBlank(message = "studentId is required")
    @Size(min = 2, max = 64, message = "studentId must be between 2 and 64 characters")
    private String studentId;

    @NotBlank(message = "name is required")
    @Size(min = 2, max = 128, message = "name must be between 2 and 128 characters")
    private String name;

    public CreateStudentRequest() {
    }

    public CreateStudentRequest(String studentId, String name) {
        this.studentId = studentId;
        this.name = name;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
