package com.example.ptfAttendanceSystem.late;


import lombok.Data;

import java.time.LocalDate;

@Data
public class LateRequestDto {

    private String email;
    private String reason;
    private LocalDate date;

    public LateRequestDto(String email, String reason, LocalDate date) {
        this.email = email;
        this.reason = reason;
        this.date = date;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
}
