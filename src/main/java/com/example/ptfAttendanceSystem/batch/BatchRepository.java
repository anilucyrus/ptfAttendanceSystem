package com.example.ptfAttendanceSystem.batch;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BatchRepository extends JpaRepository<BatchModel, Long> {

    Optional<BatchModel> findByBatchName(String batchName);
    Optional<BatchModel> findById(Long id);

}
