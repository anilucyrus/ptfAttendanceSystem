package com.example.ptfAttendanceSystem.leave;


import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequestModel, Long> {

    List<LeaveRequestModel> findByUserId(Long userId);

    List<LeaveRequestModel> findByFromDate(LocalDate fromDate);

    List<LeaveRequestModel> findByStatus(LeaveRequestStatus status);

    List<LeaveRequestModel> findByFromDateBetween(LocalDate startDate, LocalDate endDate);

    @Transactional
    void deleteByFromDateBetween(LocalDate startDate, LocalDate endDate);
}
