package com.example.ptfAttendanceSystem.late_table;

import jakarta.persistence.*;
import lombok.Data;


import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "late_attendance")
@Data
public class LateAttendance {

   @Id

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "user_name", nullable = false)
    private String userName;

    @Column(name = "batch_type", nullable = false)
    private String batchType;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Column(name = "scan_in_time", nullable = false)
    private LocalTime scanInTime;

    @Column(name = "reason_for_lateness")
    private String reasonForLateness;

    @Column(name = "status", nullable = false)
    private String status;

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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getBatchType() {
        return batchType;
    }

    public void setBatchType(String batchType) {
        this.batchType = batchType;
    }

    public LocalDate getAttendanceDate() {
        return attendanceDate;
    }

    public void setAttendanceDate(LocalDate attendanceDate) {
        this.attendanceDate = attendanceDate;
    }

    public LocalTime getScanInTime() {
        return scanInTime;
    }

    public void setScanInTime(LocalTime scanInTime) {
        this.scanInTime = scanInTime;
    }

    public String getReasonForLateness() {
        return reasonForLateness;
    }

    public void setReasonForLateness(String reasonForLateness) {
        this.reasonForLateness = reasonForLateness;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
