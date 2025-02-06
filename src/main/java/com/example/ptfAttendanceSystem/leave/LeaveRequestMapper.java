package com.example.ptfAttendanceSystem.leave;


public class LeaveRequestMapper {
    public static LeaveRequestResponseDto toResponseDto(LeaveRequestModel leaveRequest) {
        return new LeaveRequestResponseDto(
                leaveRequest.getId(),
                leaveRequest.getUserId(),
                leaveRequest.getName(),
                leaveRequest.getBatchId(),
                leaveRequest.getLeaveType(),
                leaveRequest.getReason(),
                leaveRequest.getFromDate(),
                leaveRequest.getToDate(),
                leaveRequest.getNumberOfDays(),
                leaveRequest.getStatus()
        );
    }
}

