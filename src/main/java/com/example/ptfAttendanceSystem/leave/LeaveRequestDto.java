package com.example.ptfAttendanceSystem.leave;


import lombok.Data;

import java.time.LocalDate;

@Data
public class LeaveRequestDto {

    private String leaveType; // Casual or Sick
    private String reason;
    private LocalDate fromDate;
    private LocalDate toDate;
    private int numberOfDays;

    public LeaveRequestDto(String leaveType, String reason, LocalDate fromDate, LocalDate toDate, int numberOfDays) {
        this.leaveType = leaveType;
        this.reason = reason;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.numberOfDays = numberOfDays;
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
}
