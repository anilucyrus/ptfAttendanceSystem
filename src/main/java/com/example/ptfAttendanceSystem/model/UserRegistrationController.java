package com.example.ptfAttendanceSystem.model;




import com.example.ptfAttendanceSystem.attendance.Attendance;
import com.example.ptfAttendanceSystem.attendance.AttendanceRepository;

import com.example.ptfAttendanceSystem.batch.BatchRepository;
import com.example.ptfAttendanceSystem.late.*;
import com.example.ptfAttendanceSystem.leave.*;
import com.example.ptfAttendanceSystem.qr.QRCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@RestController
@CrossOrigin
@RequestMapping(path = "/UserReg")
public class UserRegistrationController {

    @Autowired
    private  UsersRepository usersRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private QRCodeService qrCodeService;


    @Autowired
    private final UsersService usersService;

    @Autowired
    private final LeaveRequestService leaveRequestService;

    @Autowired
    private final LateRequestService lateRequestService;

    @Autowired
    private BatchRepository batchRepository;

    public UserRegistrationController(UsersService usersService,
                                      LeaveRequestService leaveRequestService,
                                      LateRequestService lateRequestService) {
        this.usersService = usersService;
        this.leaveRequestService = leaveRequestService;
        this.lateRequestService = lateRequestService;
    }

    private int scanCount = 0;
    private String currentUUID = UUID.randomUUID().toString();

    @PostMapping(path = "/reg")
    public ResponseEntity<?> registration(@RequestBody UserDto userDto) {
        try {
            return usersService.userRegistration(userDto);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong");
        }
    }

    @PostMapping(path = "/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginDto loginDto) {
        try {
            Optional<UsersModel> user = usersService.findByEmailAndPassword(loginDto.getEmail(), loginDto.getPassword());
            if (user.isPresent()) {
                UsersModel userModel = user.get();
                userModel.setToken(UUID.randomUUID().toString());
                usersService.updateUserToken(userModel);
                LoginResponseDto responseDto = new LoginResponseDto(
                        userModel.getUserId(),
                        userModel.getEmail(),
                        userModel.getName(),
                        userModel.getBatchId(),
                        userModel.getToken(),
                        "Login Successfully"
                );
                return ResponseEntity.accepted().body(responseDto);
            } else {
                return ResponseEntity.badRequest().body("No details found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong");
        }
    }

    @PostMapping(path = "/inScanQR")
    public ResponseEntity<?> scanIn(@RequestParam Long userId, @RequestBody InScanDto inScanDto) {
        try {
            ResponseEntity<?> response = usersService.scanInAndOut(userId, inScanDto);
            qrCodeService.setStatusFlag(1);

            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(path = "/outScanQR")
    public ResponseEntity<?> handleScanOut(@RequestParam Long userId, @RequestBody InScanDto inScanDto) {
        try {
            return usersService.scanInAndOut(userId, inScanDto);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/attendance")
    public ResponseEntity<?> getAttendance(@RequestParam Long userId, @RequestParam("date") String date) {
        LocalDate attendanceDate = LocalDate.parse(date);
        Optional<Attendance> attendance = attendanceRepository.findByUserIdAndAttendanceDate(userId, attendanceDate);

        if (attendance.isPresent()) {
            return new ResponseEntity<>(attendance.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Attendance not found for user on this date", HttpStatus.NOT_FOUND);
        }
    }


    @GetMapping("/attendance/date/{date}")
    public ResponseEntity<?> getAllUserAttendance(@PathVariable String date) {
        LocalDate attendanceDate = LocalDate.parse(date);
        List<Attendance> allAttendance = attendanceRepository.findByAttendanceDate(attendanceDate);

        if (allAttendance.isEmpty()) {
            return new ResponseEntity<>("No attendance records found for this date", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(allAttendance, HttpStatus.OK);
    }


    @GetMapping("/attendance/today")
    public ResponseEntity<?> getAllUserAttendanceToday() {
        LocalDate currentDate = LocalDate.now();
        List<Attendance> allAttendance = attendanceRepository.findByAttendanceDate(currentDate);

        if (allAttendance.isEmpty()) {
            return new ResponseEntity<>("No attendance records found for today", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(allAttendance, HttpStatus.OK);
    }



    @GetMapping("/attendance/month/{userId}")
    public ResponseEntity<?> getAttendanceForMonth(@PathVariable Long userId,
                                                   @RequestParam("month") int month,
                                                   @RequestParam("year") int year) {

        if (month < 1 || month > 12) {
            return new ResponseEntity<>("Invalid month", HttpStatus.BAD_REQUEST);
        }

        List<Attendance> attendanceList = usersService.getAttendanceForMonth(userId, month, year);

        if (attendanceList.isEmpty()) {
            return new ResponseEntity<>("No attendance records found for user in this month", HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(attendanceList, HttpStatus.OK);
    }

    @PostMapping("/leave-request")
    public ResponseEntity<?> requestLeave(@RequestParam Long userId, @RequestBody LeaveRequestDto leaveRequestDto) {
        return leaveRequestService.requestLeave(userId, leaveRequestDto);
    }

    @PutMapping("/leave-request")
    public ResponseEntity<?> updateLeaveRequest(@RequestParam Long requestId, @RequestBody LeaveRequestDto leaveRequestDto) {
        return leaveRequestService.updateLeaveRequest(requestId, leaveRequestDto);
    }


    @GetMapping("/leave-requests")
    public ResponseEntity<?> getAllLeaveRequestsByUserId(@RequestParam Long userId) {
        return leaveRequestService.getAllLeaveRequestsByUserId(userId);
    }


    @DeleteMapping("/leave-request")
    public ResponseEntity<?> deleteLeaveRequest(@RequestParam Long requestId) {
        return leaveRequestService.deleteLeaveRequest(requestId);
    }

    @PostMapping("/late-request")
    public ResponseEntity<?> requestLate(@RequestParam Long userId, @RequestBody LateRequestDto lateRequestDto) {
        return lateRequestService.requestLate(userId, lateRequestDto);
    }

    @PutMapping("/late-request")
    public ResponseEntity<?> updateLateRequest(@RequestParam Long requestId, @RequestBody LateRequestDto lateRequestDto) {
        return lateRequestService.updateLateRequest(requestId, lateRequestDto);
    }

    @GetMapping("/late-requests")
    public ResponseEntity<?> getAllLateRequestsForUser(@RequestParam Long userId) {
        return lateRequestService.getAllLateRequestsForUser(userId);
    }

    @DeleteMapping("/late-request")
    public ResponseEntity<?> deleteLateRequest(@RequestParam Long requestId) {
        return lateRequestService.deleteLateRequest(requestId);
    }

    @GetMapping(path = "/get/all")
    public ResponseEntity<List<UsersModel>> getAllUsers() {
        try {
            List<UsersModel> users = usersService.getAllUsers();
            return new ResponseEntity<>(users, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Internal Server Error");
        }
    }



    @PutMapping(path = "/updatePassword")
    public ResponseEntity<?> updateUserPassword(@RequestParam Long id, @RequestBody UserDto userDto) {
        try {
            UsersModel updatedUser = usersService.updateUserPassword(id, userDto);
            return new ResponseEntity<>(updatedUser, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @DeleteMapping(path = "/delete")
    public ResponseEntity<?> deleteUser(@RequestParam Long id) {
        try {
            boolean isDeleted = usersService.deleteUser(id);
            if (isDeleted) {
                return new ResponseEntity<>("User deleted successfully", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
    }



    @PutMapping(path = "/update")
    public ResponseEntity<?> updateUser(@RequestParam Long id, @RequestBody UserDto userDto) {
        try {
            return usersService.updateUser(id, userDto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Something went wrong " + e.getMessage());
        }
    }


    @PostMapping(path = "/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordDto forgotPasswordDto) {
        try {

            if (!forgotPasswordDto.getNewPassword().equals(forgotPasswordDto.getConfirmPassword())) {
                return new ResponseEntity<>("Passwords do not match", HttpStatus.BAD_REQUEST);
            }

            Optional<UsersModel> userOptional = usersRepository.findByEmail(forgotPasswordDto.getEmail());
            if (userOptional.isEmpty()) {
                return new ResponseEntity<>("User not found with the provided email", HttpStatus.NOT_FOUND);
            }

            UsersModel user = userOptional.get();
            user.setPassword(forgotPasswordDto.getNewPassword());
            usersRepository.save(user);

            return new ResponseEntity<>("Password updated successfully", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}

