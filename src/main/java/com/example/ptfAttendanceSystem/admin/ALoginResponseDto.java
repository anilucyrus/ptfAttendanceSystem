package com.example.ptfAttendanceSystem.admin;




import lombok.Data;

@Data
public class ALoginResponseDto {
    private Long id;
    private String email;
    private String name;
    private String token;
    private String message;

    public ALoginResponseDto(Long id, String email, String name, String token, String message) {
        this.id = id;
        this.email = email;
        this.name = name;
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
