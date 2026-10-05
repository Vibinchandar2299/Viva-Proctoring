package com.airouteviva.dto.response;

import java.time.Instant;

public class StudentResponse {
    private String studentId;
    private String name;
    private Instant createdAt;

    public StudentResponse() {
    }

    public StudentResponse(String studentId, String name, Instant createdAt) {
        this.studentId = studentId;
        this.name = name;
        this.createdAt = createdAt;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
