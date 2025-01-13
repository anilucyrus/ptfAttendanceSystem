package com.example.ptfAttendanceSystem.late;


import lombok.Data;

import java.time.LocalDate;

@Data
public class LateRequestResponseDto {

    private Long userId;
    private String name;
    private String email;
    private String batch;
    private String reason;
    private LocalDate date;
    private String status;

    public LateRequestResponseDto(Long userId, String name, String email, String batch, String reason, LocalDate date, String status) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.batch = batch;
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

    public String getBatch() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch;
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