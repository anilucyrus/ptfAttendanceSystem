package com.example.ptfAttendanceSystem.batchType;



import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "batchType")
@Data
public class BatchTypeModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batchType", nullable = false, unique = true)
    private String batchType;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBatchType() {
        return batchType;
    }

    public void setBatchType(String batchType) {
        this.batchType = batchType;
    }
}
