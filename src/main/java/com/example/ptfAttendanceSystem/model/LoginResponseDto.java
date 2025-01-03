package com.example.ptfAttendanceSystem.model;
public class LoginResponseDto {
    private Long id;
    private String email;
    private String name;
    private String batch;
    private String token;
    private String message;


    public LoginResponseDto(Long id, String email, String name, String batch, String token, String message) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.batch = batch;
        this.token = token;
        this.message = message;
    }

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

    public String getBatch() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch;
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
}