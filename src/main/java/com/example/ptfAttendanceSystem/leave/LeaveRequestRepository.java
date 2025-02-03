package com.example.ptfAttendanceSystem.leave;


import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("SELECT COUNT(l) > 0 FROM LeaveRequestModel l WHERE l.userId = :userId AND l.fromDate <= :toDate AND l.toDate >= :fromDate")
    boolean existsByUserIdAndFromDateLessThanEqualAndToDateGreaterThanEqual(@Param("userId") Long userId, @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);


}
