package com.example.ptfAttendanceSystem.leaveAndWFH;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LeaveWfhRepo extends JpaRepository<LeaveWfh,Integer> {

    Optional<LeaveWfh> findByName(String name);
}
