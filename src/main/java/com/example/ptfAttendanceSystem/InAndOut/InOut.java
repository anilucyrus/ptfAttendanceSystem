package com.example.ptfAttendanceSystem.InAndOut;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "InAndOut")
@Data
public class InOut {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "user_name", nullable = false)
    private String userName;

    @Column(name = "batch_id")
    private Long batchId;

    @Column(name = "batchName", nullable = false)
    private String batchName;

    @Column(name = "attendance_date")
    private LocalDate attendanceDate;


    @Column(name = "scan_in_time1")
    private LocalTime scanInTime1;

    @Column(name = "scan_out_time1")
    private LocalTime scanOutTime1;


    @Column(name = "scan_in_time2")
    private LocalTime scanInTime2;

    @Column(name = "scan_out_time2")
    private LocalTime scanOutTime2;

    @Column(name = "scan_in_time3")
    private LocalTime scanInTime3;

    @Column(name = "scan_out_time3")
    private LocalTime scanOutTime3;


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

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
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

    public LocalTime getScanInTime1() {
        return scanInTime1;
    }

    public void setScanInTime1(LocalTime scanInTime1) {
        this.scanInTime1 = scanInTime1;
    }

    public LocalTime getScanOutTime1() {
        return scanOutTime1;
    }

    public void setScanOutTime1(LocalTime scanOutTime1) {
        this.scanOutTime1 = scanOutTime1;
    }

    public LocalTime getScanInTime2() {
        return scanInTime2;
    }

    public void setScanInTime2(LocalTime scanInTime2) {
        this.scanInTime2 = scanInTime2;
    }

    public LocalTime getScanOutTime2() {
        return scanOutTime2;
    }

    public void setScanOutTime2(LocalTime scanOutTime2) {
        this.scanOutTime2 = scanOutTime2;
    }

    public LocalTime getScanInTime3() {
        return scanInTime3;
    }

    public void setScanInTime3(LocalTime scanInTime3) {
        this.scanInTime3 = scanInTime3;
    }

    public LocalTime getScanOutTime3() {
        return scanOutTime3;
    }

    public void setScanOutTime3(LocalTime scanOutTime3) {
        this.scanOutTime3 = scanOutTime3;
    }
}