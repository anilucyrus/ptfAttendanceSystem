package com.example.ptfAttendanceSystem.leave;




import lombok.Data;
import java.time.LocalDate;

@Data
public class LeaveRequestResponseDto {
    private Long id;
    private Long userId;
    private String name;
    private Long batchId;
    private String leaveType;
    private String reason;
    private LocalDate fromDate;
    private LocalDate toDate;
    private int numberOfDays;
    private LeaveRequestStatus status;


    public LeaveRequestResponseDto() {
    }

    public LeaveRequestResponseDto(Long id, Long userId, String name, Long batchId, String leaveType, String reason, LocalDate fromDate, LocalDate toDate, int numberOfDays, LeaveRequestStatus status) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.batchId = batchId;
        this.leaveType = leaveType;
        this.reason = reason;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.numberOfDays = numberOfDays;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public LeaveRequestStatus getStatus() {
        return status;
    }

    public void setStatus(LeaveRequestStatus status) {
        this.status = status;
    }
}
