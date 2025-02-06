package com.example.ptfAttendanceSystem.qr;

import lombok.Data;
import lombok.Data;

@Data
public class AlreadyScanResponceDto {
    private String message;

    public AlreadyScanResponceDto(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}