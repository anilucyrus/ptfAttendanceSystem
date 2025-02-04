package com.example.ptfAttendanceSystem.batch;

import jakarta.persistence.*;

import java.time.LocalTime;

@Entity
@Table(name = "batch")
public class BatchData {




    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "batch")
    private String batch;

    @Column(name = "startingTime")
    private LocalTime startingTime;

    @Column(name = "endingTime")
    private LocalTime endingTime;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getBatch() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch;
    }

    public LocalTime getStartingTime() {
        return startingTime;
    }

    public void setStartingTime(LocalTime startingTime) {
        this.startingTime = startingTime;
    }

    public LocalTime getEndingTime() {
        return endingTime;
    }

    public void setEndingTime(LocalTime endingTime) {
        this.endingTime = endingTime;
    }
}

