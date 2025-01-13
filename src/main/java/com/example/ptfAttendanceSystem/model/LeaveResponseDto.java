package com.example.ptfAttendanceSystem.model;



import com.example.ptfAttendanceSystem.leave.LeaveRequestStatus;

import java.time.LocalDate;

public class LeaveResponseDto {
    private Long userId;
    private String userName;
    private String batch;
    private String leaveType; // Casual or Sick
    private String reason;
    private LocalDate fromDate;
    private LocalDate toDate;
    private int numberOfDays;
    private String status;

    public LeaveResponseDto(Long userId, String userName, String batch, String leaveType, String reason, LocalDate fromDate, LocalDate toDate, int numberOfDays, String status) {
        this.userId = userId;
        this.userName = userName;
        this.batch = batch;
        this.leaveType = leaveType;
        this.reason = reason;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.numberOfDays = numberOfDays;
        this.status = status;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getBatch() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch;
    }

    public String getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(String leaveType) {
        this.leaveType = leaveType;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public void setToDate(LocalDate toDate) {
        this.toDate = toDate;
    }

    public int getNumberOfDays() {
        return numberOfDays;
    }

    public void setNumberOfDays(int numberOfDays) {
        this.numberOfDays = numberOfDays;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}