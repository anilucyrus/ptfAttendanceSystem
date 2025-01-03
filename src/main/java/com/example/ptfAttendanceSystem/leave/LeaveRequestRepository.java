package com.example.ptfAttendanceSystem.leave;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequestModel, Long> {
    List<LeaveRequestModel> findByUserId(Long userId);

    List<LeaveRequestModel> findByFromDate(LocalDate fromDate);

    List<LeaveRequestModel> findByStatus(LeaveRequestStatus status);
}
