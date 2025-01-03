package com.example.ptfAttendanceSystem.batch;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BatchRepo extends JpaRepository<BatchData,Integer> {

    Optional<BatchData> findById(Long id);


}