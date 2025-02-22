package com.example.ptfAttendanceSystem.model;






import com.example.ptfAttendanceSystem.admin.AdminService;
import com.example.ptfAttendanceSystem.attendance.Attendance;
import com.example.ptfAttendanceSystem.attendance.AttendanceRepository;

import com.example.ptfAttendanceSystem.batch.BatchRepository;
import com.example.ptfAttendanceSystem.late.*;
import com.example.ptfAttendanceSystem.leave.*;
import com.example.ptfAttendanceSystem.leaveAndWFH.LeaveWfh;
import com.example.ptfAttendanceSystem.leaveAndWFH.LeaveWfhRepo;
import com.example.ptfAttendanceSystem.leaveAndWFH.Wfh;
import com.example.ptfAttendanceSystem.leaveAndWFH.WfhService;
import com.example.ptfAttendanceSystem.qr.QRCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

import java.util.*;
import java.util.stream.Collectors;


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
    private LeaveWfhRepo leaveWfhRepo;

    @Autowired
    private WfhService wfhService;
    @Autowired
    private AdminService adminService;

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
        return usersService.userRegistration(userDto);
    }

    @PostMapping(path = "/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginDto loginDto) {
        return usersService.loginUser(loginDto);
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
            return new ResponseEntity<>("Attendance not found for user on this date", HttpStatus.NO_CONTENT);
        }
    }


    @GetMapping("/attendance/date/{date}")
    public ResponseEntity<?> getAllUserAttendance(@PathVariable String date) {
        LocalDate attendanceDate = LocalDate.parse(date);
        List<Attendance> allAttendance = attendanceRepository.findByAttendanceDate(attendanceDate);

        if (allAttendance.isEmpty()) {
            return new ResponseEntity<>("No attendance records found for this date", HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(allAttendance, HttpStatus.OK);
    }


    @GetMapping("/attendance/today")
    public ResponseEntity<?> getAllUserAttendanceToday(@RequestParam Long batchId) {
        LocalDate currentDate = LocalDate.now();

        if (batchId != null && !adminService.isBatchExists(batchId)) {
            return new ResponseEntity<>("Batch not found", HttpStatus.NOT_FOUND);
        }

        List<Attendance> allAttendance = attendanceRepository.findByAttendanceDate(currentDate);

        List<Map<String, Object>> attendanceResponse = allAttendance.stream()
                .map(attendance -> {
                    Optional<UsersModel> userOpt = usersRepository.findById(attendance.getUserId());
                    if (userOpt.isPresent()) {
                        UsersModel user = userOpt.get();
                        if (user.getBatchId().equals(batchId)) {
                            Map<String, Object> responseMap = new HashMap<>();
                            responseMap.put("id", attendance.getId());
                            responseMap.put("userId", attendance.getUserId());
                            responseMap.put("userName", attendance.getUserName());
                            responseMap.put("batchId", user.getBatchId());
                            responseMap.put("batchName", attendance.getBatchName());
                            responseMap.put("attendanceDate", attendance.getAttendanceDate());
                            responseMap.put("scanInTime", attendance.getScanInTime());
                            responseMap.put("scanOutTime", attendance.getScanOutTime());
                            responseMap.put("status", attendance.getStatus());
                            return responseMap;
                        }
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (attendanceResponse.isEmpty()) {
            return new ResponseEntity<>("No attendance records found for today", HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(attendanceResponse, HttpStatus.OK);
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
            return new ResponseEntity<>("No attendance records found for user in this month", HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(attendanceList, HttpStatus.OK);
    }

    @PostMapping("/leave-request")
    public ResponseEntity<?> requestLeave(@RequestParam Long userId, @RequestBody LeaveRequestDto leaveRequestDto,
                                          @RequestParam Integer wfh) {
        Optional<LeaveWfh> leaveWfhOptional = leaveWfhRepo.findById(wfh);
        if (leaveWfhOptional.isPresent()){
            LeaveWfh leaveWfh = leaveWfhOptional.get();
            String name = leaveWfh.getName();
            if ("Leave".equalsIgnoreCase(name)){
                return leaveRequestService.requestLeave(userId, leaveRequestDto);
            } else if ("Work From Home".equalsIgnoreCase(name)) {
                return leaveRequestService.requestWorkFromHome(userId, leaveRequestDto);
            }else {
                return new ResponseEntity<>("Invalaid Name of leave or WFH",HttpStatus.NOT_FOUND);
            }
        }else {
            return new ResponseEntity<>("Leave or WFH is required",HttpStatus.BAD_REQUEST);
        }

    }

//    @PutMapping("/leave-request")
//    public ResponseEntity<?> updateLeaveRequest(@RequestParam Long requestId, @RequestBody LeaveRequestDto leaveRequestDto) {
//        return leaveRequestService.updateLeaveRequest(requestId, leaveRequestDto);
//    }


    @GetMapping("/leave-requests")
    public ResponseEntity<?> getAllLeaveRequestsByUserId(@RequestParam Long userId,@RequestParam Integer wfromHId) {



        Optional<LeaveWfh> leaveWfhOptional = leaveWfhRepo.findById(wfromHId);
        if (leaveWfhOptional.isPresent()){
            LeaveWfh leaveWfh = leaveWfhOptional.get();
            String name = leaveWfh.getName();
            if ("Leave".equalsIgnoreCase(name)){
                return leaveRequestService.getAllLeaveRequestsByUserId(userId);
            } else if ("Work From Home".equalsIgnoreCase(name)) {
                return leaveRequestService.getAllWorkFromHomeRequestsByUserId(userId);
            }else {
                return new ResponseEntity<>("Invalaid Name of leave or WFH",HttpStatus.NOT_FOUND);
            }
        }else {
            return new ResponseEntity<>("Leave or WFH is required",HttpStatus.BAD_REQUEST);
        }
    }


    @DeleteMapping("/leave-request")
    public ResponseEntity<?> deleteLeaveRequest(@RequestParam Long requestId) {
        return leaveRequestService.deleteLeaveRequest(requestId);
    }

    @PostMapping("/late-request")
    public ResponseEntity<?> requestLate(@RequestParam Long userId, @RequestBody LateRequestDto lateRequestDto) {
        return lateRequestService.requestLate(userId, lateRequestDto);
    }
//
//    @PutMapping("/late-request")
//    public ResponseEntity<?> updateLateRequest(@RequestParam Long requestId, @RequestBody LateRequestDto lateRequestDto) {
//        return lateRequestService.updateLateRequest(requestId, lateRequestDto);
//    }

    @GetMapping("/late-requests")
    public ResponseEntity<?> getAllLateRequestsForUser(@RequestParam Long userId) {
        return lateRequestService.getAllLateRequestsForUser(userId);
    }

    @DeleteMapping("/late-request")
    public ResponseEntity<?> deleteLateRequest(@RequestParam Long requestId) {
        return lateRequestService.deleteLateRequest(requestId);
    }

    @DeleteMapping("/wfh-request")
    public ResponseEntity<?> deleteWorkFromHomeRequest(@RequestParam Long requestId) {
        return leaveRequestService.deleteWorkFromHomeRequest(requestId);
    }
//
//    @DeleteMapping("/wfh-request")
//    public ResponseEntity<?> deleteWfhRequest(@RequestParam Long requestId) {
//        return leaveRequestService.deleteWfhRequest(requestId);
//    }

    @GetMapping("/getAllUsers")
    public ResponseEntity<?> getAllUsers(@RequestParam(required = false) Long batchId) {
        try {
            if (batchId != null && !usersService.isBatchExists(batchId)) {
                return new ResponseEntity<>("Batch not found", HttpStatus.NOT_FOUND);
            }
            List<GetAllUsersDTO> users = usersService.getAllUsers(batchId);
            if (users.isEmpty()) {
                return new ResponseEntity<>("User not found", HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(users, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(Collections.emptyList(), HttpStatus.INTERNAL_SERVER_ERROR);
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


//    @DeleteMapping(path = "/delete")
//    public ResponseEntity<?> deleteUser(@RequestParam Long id) {
//        try {
//            boolean isDeleted = usersService.deleteUser(id);
//            if (isDeleted) {
//                return new ResponseEntity<>("User deleted successfully", HttpStatus.OK);
//            } else {
//                return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
//    }



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

