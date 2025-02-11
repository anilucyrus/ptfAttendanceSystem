package com.example.ptfAttendanceSystem.model;


import com.example.ptfAttendanceSystem.batch.BatchModel;

public class UserMapper {
    public static URegistrationResponse toResponse(UsersModel user, BatchModel batch) {
        return new URegistrationResponse(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getBatchId(),
                batch != null ? batch.getBatchName() : null,
                user.getPhoneNumber()
        );
    }
}

