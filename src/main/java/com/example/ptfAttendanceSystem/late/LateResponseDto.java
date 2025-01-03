package com.example.ptfAttendanceSystem.late;

import java.time.LocalDate;

public class LateResponseDto {
    private Long userId;
    private String userName;
    private String batch;
    private String reason;
    private LocalDate date;
    private String status;

    public LateResponseDto(Long userId, String userName, String batch, String reason, LocalDate date, String status) {
        this.userId = userId;
        this.userName = userName;
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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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