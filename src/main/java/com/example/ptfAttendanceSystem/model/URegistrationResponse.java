package com.example.ptfAttendanceSystem.model;

import lombok.Data;

@Data
public class URegistrationResponse {
    private Long userId;
    private String name;
    private String email;
    private String batch;
    private String phoneNumber;

    public URegistrationResponse(Long userId, String name, String email, String batch, String phoneNumber) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.batch = batch;
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

    public String getBatch() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
