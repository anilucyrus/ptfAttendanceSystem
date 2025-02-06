package com.example.ptfAttendanceSystem.late;


import lombok.Data;

import java.time.LocalDate;

@Data
public class LateRequestResponseDto {

    private Long userId;
    private String name;
    private String email;
    private Long batchId;
    private String batchName;
    private String reason;
    private LocalDate date;
    private String status;

    public LateRequestResponseDto(Long userId, String name, String email, Long batchId, String batchName, String reason, LocalDate date, String status) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.batchId = batchId;
        this.batchName = batchName;
        this.reason = reason;
        this.date = date;
        this.status = status;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }

    public String getBatchName() {
        return batchName;
    }

    public void setBatchName(String batchName) {
        this.batchName = batchName;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}