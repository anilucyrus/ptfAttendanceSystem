package com.example.ptfAttendanceSystem.model;



import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor

public class LoginResponseDto {
    private Long id;
    private String email;
    private String name;
    private Long batchId;
    private String token;
    private String message;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LoginResponseDto(Long id, String email, String name, Long batchId, String token, String message) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.batchId = batchId;
        this.token = token;
        this.message = message;



    }
}