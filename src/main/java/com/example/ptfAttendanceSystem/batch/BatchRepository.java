package com.example.ptfAttendanceSystem.batch;



import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BatchRepository extends JpaRepository<BatchModel, Long> {

    Optional<BatchModel> findByBatchName(String batchName);
    Optional<BatchModel> findById(Long id);
    List<BatchModel> findByBatchType_Id(Long batchTypeId); // Fetch batches by batch type ID

}
