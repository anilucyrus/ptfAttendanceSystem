package com.example.ptfAttendanceSystem.batchType;



import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BatchTypeRepository extends JpaRepository<BatchTypeModel, Long> {

    Optional<BatchTypeModel> findById(Long id);

    Optional<BatchTypeModel> findByBatchType(String batchType);
}


