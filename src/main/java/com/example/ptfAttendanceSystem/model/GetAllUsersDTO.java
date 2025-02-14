package com.example.ptfAttendanceSystem.model;

import lombok.Data;

@Data

public class GetAllUsersDTO {
    private Long userId;
    private String name;
    private String email;
    private String phoneNumber;
    private Long batchId;
    private String batchName;

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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
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

    public GetAllUsersDTO(Long userId, String name, String email, String phoneNumber, Long batchId, String batchName) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.batchId = batchId;
        this.batchName = batchName;
    }
}
