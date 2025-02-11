package com.example.ptfAttendanceSystem.model;

import lombok.Data;

@Data
public class URegistrationResponse {
    private Long userId;
    private String name;
    private String email;
    private Long batchId;
    private String batchName;
    private String phoneNumber;

    public URegistrationResponse(Long userId, String name, String email, Long batchId, String batchName, String phoneNumber) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.batchId = batchId;
        this.batchName = batchName;
        this.phoneNumber = phoneNumber;
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}