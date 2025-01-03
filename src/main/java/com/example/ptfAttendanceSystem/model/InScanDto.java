package com.example.ptfAttendanceSystem.model;


import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class InScanDto {
    private LocalDate presentDate;
    private LocalTime presentTime;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
