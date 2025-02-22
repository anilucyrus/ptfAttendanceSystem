package com.example.ptfAttendanceSystem.leaveAndWFH;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface WfhRepo extends JpaRepository<Wfh,Long> {
    List<Wfh> findByUserId(Long userId);

    boolean existsByUserIdAndFromDateLessThanEqualAndToDateGreaterThanEqual(Long userId, LocalDate toDate, LocalDate fromDate);
    List<Wfh> findByStatusAndBatchId(WfhStatus status, Long batchId);

}