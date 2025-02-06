package com.example.ptfAttendanceSystem.model;
import lombok.Data;

@Data

public class UpdatePasswordDto {
    private String password;

    public UpdatePasswordDto(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
