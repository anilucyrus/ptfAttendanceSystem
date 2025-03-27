package com.example.ptfAttendanceSystem.batch;




import com.example.ptfAttendanceSystem.batchType.BatchTypeModel;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalTime;

@Entity
@Table(name = "batch")
@Data
public class BatchModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_name", nullable = false, unique = true)
    private String batchName;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "batch_latitude", nullable = false)
    private String batchLatitude;

    @Column(name = "batch_longitude", nullable = false)
    private String batchLongitude;

    @ManyToOne
    @JoinColumn(name = "batch_type_id", nullable = false)
    private BatchTypeModel batchType;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBatchName() {
        return batchName;
    }

    public void setBatchName(String batchName) {
        this.batchName = batchName;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getBatchLatitude() {
        return batchLatitude;
    }

    public void setBatchLatitude(String batchLatitude) {
        this.batchLatitude = batchLatitude;
    }

    public String getBatchLongitude() {
        return batchLongitude;
    }

    public void setBatchLongitude(String batchLongitude) {
        this.batchLongitude = batchLongitude;
    }

    public BatchTypeModel getBatchType() {
        return batchType;
    }

    public void setBatchType(BatchTypeModel batchType) {
        this.batchType = batchType;
    }
}
