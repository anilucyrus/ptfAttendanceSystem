package com.example.ptfAttendanceSystem.model;

public class UserMapper {
    public static URegistrationResponse toResponse(UsersModel user) {
        return new URegistrationResponse(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getBatchId(),
                user.getPhoneNumber()
        );
    }
}
