package com.example.ptfAttendanceSystem.qr;


import lombok.Data;

@Data
public class   ScanResponseDto {
    private Long userId;
    private String name;
    private String batch;
    private String scanType;
    private String date;
    private String time;
    private String message;

    public ScanResponseDto(Long userId, String scanType, String date, String time, String message) {
        this.userId = userId;
        this.scanType = scanType;
        this.date = date;
        this.time = time;
        this.message = message;
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

    public String getBatch() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch;
    }

    public String getScanType() {
        return scanType;
    }

    public void setScanType(String scanType) {
        this.scanType = scanType;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

