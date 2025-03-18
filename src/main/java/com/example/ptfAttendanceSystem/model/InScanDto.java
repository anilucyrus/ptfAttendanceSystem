package com.example.ptfAttendanceSystem.model;



import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
@Data
public class InScanDto {
    private LocalDate presentDate;
    private LocalTime presentTime;
    private String userLatitude;
    private String userLongitude;
    private String type;

    public LocalDate getPresentDate() {
        return presentDate;
    }

    public void setPresentDate(LocalDate presentDate) {
        this.presentDate = presentDate;
    }

    public LocalTime getPresentTime() {
        return presentTime;
    }

    public void setPresentTime(LocalTime presentTime) {
        this.presentTime = presentTime;
    }

    public String getUserLatitude() {
        return userLatitude;
    }

    public void setUserLatitude(String userLatitude) {
        this.userLatitude = userLatitude;
    }

    public String getUserLongitude() {
        return userLongitude;
    }

    public void setUserLongitude(String userLongitude) {
        this.userLongitude = userLongitude;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }}