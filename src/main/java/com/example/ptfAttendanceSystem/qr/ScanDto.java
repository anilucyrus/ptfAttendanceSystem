package com.example.ptfAttendanceSystem.qr;

import lombok.Data;

@Data
public class ScanDto {
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}