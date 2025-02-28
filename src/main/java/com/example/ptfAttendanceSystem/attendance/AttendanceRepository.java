package com.example.ptfAttendanceSystem.attendance;


import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {


    Optional<Attendance> findByUserIdAndAttendanceDate(Long userId, LocalDate date);
    List<Attendance> findByAttendanceDate(LocalDate date);
    List<Attendance> findByUserIdAndAttendanceDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
    Optional<Attendance> findByUserId(Long userId);
    List<Attendance> findByAttendanceDateBetween(LocalDate startDate, LocalDate endDate);


    @Transactional
    @Modifying
    void deleteByAttendanceDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.userId = :userId AND a.attendanceDate BETWEEN :startDate AND :endDate")
    int countByUserIdAndAttendanceDateBetween(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);



}