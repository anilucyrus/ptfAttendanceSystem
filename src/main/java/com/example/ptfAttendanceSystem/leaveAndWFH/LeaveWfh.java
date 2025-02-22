package com.example.ptfAttendanceSystem.leaveAndWFH;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "leave_WFH")
public class LeaveWfh {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "name")
    private String name;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
