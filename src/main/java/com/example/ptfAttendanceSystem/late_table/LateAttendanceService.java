package com.example.ptfAttendanceSystem.late_table;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class LateAttendanceService {

    @Autowired
    private LateAttendanceRepository lateAttendanceRepository;

    public void saveLateAttendance(LateAttendance lateAttendance) {
        lateAttendanceRepository.save(lateAttendance);
    }

    public List<LateAttendance> getLateAttendanceByDate(LocalDate date) {
        return lateAttendanceRepository.findByAttendanceDate(date);
    }

    public List<LateAttendance> getLateAttendanceByUser(Long userId) {
        return lateAttendanceRepository.findByUserId(userId);
    }
}
