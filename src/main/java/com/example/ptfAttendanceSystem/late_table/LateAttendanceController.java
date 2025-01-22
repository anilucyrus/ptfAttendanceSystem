package com.example.ptfAttendanceSystem.late_table;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping(path = "/LateAttendance")
public class LateAttendanceController {

    @Autowired
    private LateAttendanceService lateAttendanceService;

    @GetMapping("/byDate")
    public ResponseEntity<List<LateAttendance>> getLateAttendanceByDate(@RequestParam LocalDate date) {
        List<LateAttendance> lateAttendances = lateAttendanceService.getLateAttendanceByDate(date);
        return new ResponseEntity<>(lateAttendances, HttpStatus.OK);
    }

    @GetMapping("/byUser")
    public ResponseEntity<List<LateAttendance>> getLateAttendanceByUser(@RequestParam Long userId) {
        List<LateAttendance> lateAttendances = lateAttendanceService.getLateAttendanceByUser(userId);
        return new ResponseEntity<>(lateAttendances, HttpStatus.OK);
    }
}
