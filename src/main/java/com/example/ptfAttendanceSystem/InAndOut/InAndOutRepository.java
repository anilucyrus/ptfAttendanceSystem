package com.example.ptfAttendanceSystem.InAndOut;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface InAndOutRepository extends JpaRepository<InOut, Long> {
    List<InOut> findByUserIdAndAttendanceDate(Long userId, LocalDate attendanceDate);

    List<InOut> findByUserIdAndAttendanceDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

}
