package com.example.ptfAttendanceSystem.late;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "late_requests")
@Data
public class LateRequestModel {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String reason;
    private LocalDate date;

    @Column(name = "batch_id")
    private Long batchId;

    @Enumerated(EnumType.STRING)
    private LateRequestStatus status = LateRequestStatus.PENDING;

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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }

    public LateRequestStatus getStatus() {
        return status;
    }

    public void setStatus(LateRequestStatus status) {
        this.status = status;
    }
}
