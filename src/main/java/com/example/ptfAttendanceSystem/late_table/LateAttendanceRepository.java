package com.example.ptfAttendanceSystem.late_table;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LateAttendanceRepository extends JpaRepository<LateAttendance, Long> {
    List<LateAttendance> findByAttendanceDate(LocalDate attendanceDate);
    List<LateAttendance> findByUserId(Long userId);


}
