package com.example.ptfAttendanceSystem.attendance;



import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "attendance")
@Data
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "user_name", nullable = false)
    private String userName;

    @Column(name = "batchName", nullable = false)
    private String batchName;
    @Column(name = "attendance_date")
    private LocalDate attendanceDate;


    @Column(name = "scan_in_time")
    private LocalTime scanInTime;

    @Column(name = "scan_out_time")
    private LocalTime scanOutTime;

    @Column(name = "status")
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

    public String getBatchName() {
        return batchName;
    }

    public void setBatchName(String batchName) {
        this.batchName = batchName;
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

    public LocalTime getScanOutTime() {
        return scanOutTime;
    }

    public void setScanOutTime(LocalTime scanOutTime) {
        this.scanOutTime = scanOutTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
