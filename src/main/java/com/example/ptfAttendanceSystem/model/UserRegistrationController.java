package com.example.ptfAttendanceSystem.model;



import com.example.ptfAttendanceSystem.attendance.Attendance;
import com.example.ptfAttendanceSystem.attendance.AttendanceRepository;
import com.example.ptfAttendanceSystem.late.*;
import com.example.ptfAttendanceSystem.leave.*;
import com.example.ptfAttendanceSystem.qr.QRCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@CrossOrigin
@RequestMapping(path = "/UserReg")
public class UserRegistrationController {
    @Autowired
    private UsersService usersService;

    @Autowired
    private LateRequestRepository lateRequestRepository;

    @Autowired
    private UserDto userDto;

    @Autowired
    private  UsersRepository usersRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private QRCodeService qrCodeService;

    private int scanCount = 0;
    private String currentUUID = UUID.randomUUID().toString();

    @PostMapping(path = "/reg")
    public ResponseEntity<?> registration(@RequestBody UserDto userDto) {
        try {
            return usersService.userRegistration(userDto);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @PostMapping(path = "/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginDto loginDto) {
        try {
            Optional<UsersModel> user = usersService.findByEmailAndPassword(loginDto.getEmail(), loginDto.getPassword());
            if (user.isPresent()) {
                UsersModel userModel = user.get();
                String token = UUID.randomUUID().toString();
                userModel.setToken(token);
                usersService.updateUserToken(userModel);
                LoginResponseDto responseDto = new LoginResponseDto(
                        userModel.getUserId(),

                        userModel.getEmail(),
                        userModel.getName(),

                        userModel.getBatch(),
                        userModel.getToken(),
                        "Login Successfully"
                );


                return new ResponseEntity<>(responseDto, HttpStatus.ACCEPTED);
            } else {
                return new ResponseEntity<>("No details found", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
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
        try {

            LocalDate currentDate = LocalDate.now();
            if (leaveRequestDto.getFromDate().isBefore(currentDate) || leaveRequestDto.getToDate().isBefore(currentDate)) {
                return new ResponseEntity<>("Leave request cannot be made for past dates", HttpStatus.BAD_REQUEST);
            }


            Optional<UsersModel> user = usersService.getUserById(userId);
            if (user.isEmpty()) {
                return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
            }
            UsersModel userModel = user.get();

            if (leaveRequestDto.getFromDate().isAfter(leaveRequestDto.getToDate())) {
                return new ResponseEntity<>("From date cannot be after To date", HttpStatus.BAD_REQUEST);
            }


            boolean exists = leaveRequestRepository.existsByUserIdAndFromDateLessThanEqualAndToDateGreaterThanEqual(
                    userId, leaveRequestDto.getToDate(), leaveRequestDto.getFromDate());
            if (exists) {
                return new ResponseEntity<>("User already has a leave request for the selected date(s)", HttpStatus.BAD_REQUEST);
            }

            LeaveRequestModel leaveRequest = new LeaveRequestModel();
            leaveRequest.setUserId(userModel.getUserId());
            leaveRequest.setLeaveType(leaveRequestDto.getLeaveType());
            leaveRequest.setReason(leaveRequestDto.getReason());
            leaveRequest.setFromDate(leaveRequestDto.getFromDate());
            leaveRequest.setToDate(leaveRequestDto.getToDate());
            leaveRequest.setNumberOfDays(Period.between(leaveRequestDto.getFromDate(), leaveRequestDto.getToDate()).getDays() + 1);
            leaveRequest.setStatus(LeaveRequestStatus.PENDING);
            leaveRequest.setName(userModel.getName());
            leaveRequest.setBatch(userModel.getBatch());
            leaveRequestRepository.save(leaveRequest);

            LeaveRequestResponseDto responseDto = new LeaveRequestResponseDto(
                    leaveRequest.getId(),
                    leaveRequest.getUserId(),
                    leaveRequest.getName(),
                    leaveRequest.getBatch(),
                    leaveRequest.getLeaveType(),
                    leaveRequest.getReason(),
                    leaveRequest.getFromDate(),
                    leaveRequest.getToDate(),
                    leaveRequest.getNumberOfDays(),
                    leaveRequest.getStatus()
            );

            return new ResponseEntity<>(responseDto, HttpStatus.CREATED);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }




    @PutMapping("/leave-request")
    public ResponseEntity<?> updateLeaveRequest(@RequestParam Long requestId, @RequestBody LeaveRequestDto leaveRequestDto) {
        try {

            LocalDate currentDate = LocalDate.now();
            if (leaveRequestDto.getFromDate().isBefore(currentDate) || leaveRequestDto.getToDate().isBefore(currentDate)) {
                return new ResponseEntity<>("Leave request cannot be made for past dates", HttpStatus.BAD_REQUEST);
            }

            Optional<LeaveRequestModel> leaveRequestOptional = leaveRequestRepository.findById(requestId);
            if (leaveRequestOptional.isEmpty()) {
                return new ResponseEntity<>("Leave request not found", HttpStatus.NOT_FOUND);
            }

            LeaveRequestModel leaveRequest = leaveRequestOptional.get();

            if (leaveRequestDto.getFromDate().isAfter(leaveRequestDto.getToDate())) {
                return new ResponseEntity<>("From date cannot be after To date", HttpStatus.BAD_REQUEST);
            }


            leaveRequest.setLeaveType(leaveRequestDto.getLeaveType());
            leaveRequest.setReason(leaveRequestDto.getReason());
            leaveRequest.setFromDate(leaveRequestDto.getFromDate());
            leaveRequest.setToDate(leaveRequestDto.getToDate());
            leaveRequest.setNumberOfDays(Period.between(leaveRequestDto.getFromDate(), leaveRequestDto.getToDate()).getDays() + 1);
            leaveRequest.setStatus(LeaveRequestStatus.PENDING);

            leaveRequestRepository.save(leaveRequest);


            Optional<UsersModel> userOptional = usersService.getUserById(leaveRequest.getUserId());
            if (userOptional.isEmpty()) {
                return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
            }

            UsersModel userModel = userOptional.get();
            LeaveRequestResponseDto responseDto = new LeaveRequestResponseDto(
                    leaveRequest.getId(),
                    leaveRequest.getUserId(),
                    userModel.getName(),
                    userModel.getBatch(),
                    leaveRequest.getLeaveType(),
                    leaveRequest.getReason(),
                    leaveRequest.getFromDate(),
                    leaveRequest.getToDate(),
                    leaveRequest.getNumberOfDays(),
                    leaveRequest.getStatus()
            );


            return new ResponseEntity<>(responseDto, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @DeleteMapping("/leave-request")
    public ResponseEntity<String> deleteLeaveRequest(@RequestParam Long requestId) {
        try {

            Optional<LeaveRequestModel> leaveRequestOptional = leaveRequestRepository.findById(requestId);
            if (leaveRequestOptional.isEmpty()) {
                return new ResponseEntity<>("Leave request not found", HttpStatus.NOT_FOUND);
            }

            LeaveRequestModel leaveRequest = leaveRequestOptional.get();

            leaveRequestRepository.delete(leaveRequest);

            return new ResponseEntity<>("Leave request deleted successfully", HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Error occurred while deleting the leave request", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/leave-requests")
    public ResponseEntity<?> getLeaveRequests(@RequestParam Long userId) {
        try {
            List<LeaveRequestModel> requests = usersService.getLeaveRequestsByUserId(userId);
            if (requests.isEmpty()) {
                return new ResponseEntity<>("No leave requests found for the user", HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(requests, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    @PostMapping("/late-request")
    public ResponseEntity<?> requestLate(@RequestParam Long userId, @RequestBody LateRequestDto lateRequestDto) {
        try {

            Optional<UsersModel> user = usersService.findByEmail(lateRequestDto.getEmail());

            if (user.isEmpty()) {
                return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
            }
            UsersModel userModel = user.get();
            LocalDate requestedDate = lateRequestDto.getDate();
            LocalDate currentDate = LocalDate.now();
            LocalTime currentTime = LocalTime.now();


            Optional<LateRequestModel> existingRequest = lateRequestRepository.findByUserIdAndDate(userModel.getUserId(), requestedDate);
            if (existingRequest.isPresent()) {
                return new ResponseEntity<>("You have already submitted a late request for today.", HttpStatus.BAD_REQUEST);
            }


            String batch = userModel.getBatch();
            LocalTime allowedTime = null;

            if ("morning batch".equalsIgnoreCase(batch)) {
                allowedTime = LocalTime.of(9, 30);
            } else if ("evening batch".equalsIgnoreCase(batch)) {
                allowedTime = LocalTime.of(13, 30);
            }
            else if ("regular batch".equalsIgnoreCase(batch)) {
                allowedTime = LocalTime.of(9, 30);
            }
            else {
                return new ResponseEntity<>("Invalid batch", HttpStatus.BAD_REQUEST);
            }


            if (requestedDate.isBefore(currentDate)) {
                return new ResponseEntity<>("Late request cannot be made for a past date", HttpStatus.BAD_REQUEST);
            }

            if (currentTime.isAfter(allowedTime)) {
                String errorMessage = "Late request not allowed for this batch after " + allowedTime.toString();
                return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
            }

            LateRequestModel lateRequest = new LateRequestModel();
            lateRequest.setUserId(userModel.getUserId());
            lateRequest.setReason(lateRequestDto.getReason());
            lateRequest.setDate(lateRequestDto.getDate());
            lateRequest.setStatus(LateRequestStatus.PENDING);
            lateRequest.setName(userModel.getName());
            lateRequest.setBatch(userModel.getBatch());


            lateRequestRepository.save(lateRequest);


            LateRequestResponseDto responseDto = new LateRequestResponseDto(
                    userModel.getUserId(),
                    userModel.getName(),
                    userModel.getEmail(),
                    userModel.getBatch(),
                    lateRequestDto.getReason(),
                    lateRequestDto.getDate(),
                    lateRequest.getStatus().name()
            );

            return new ResponseEntity<>(responseDto, HttpStatus.CREATED);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @DeleteMapping("/late-request")
    public ResponseEntity<?> deleteLateRequest(@RequestParam Long id) {
        try {
            Optional<LateRequestModel> lateRequest = lateRequestRepository.findById(id);

            if (lateRequest.isEmpty()) {
                return new ResponseEntity<>("Late request not found", HttpStatus.NOT_FOUND);
            }

            lateRequestRepository.deleteById(id);
            return new ResponseEntity<>("Late request deleted successfully", HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed to delete late request", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    @PutMapping("/late-request")
    public ResponseEntity<?> updateLateRequest(@RequestParam Long requestId, @RequestBody LateRequestDto lateRequestDto) {
        try {

            Optional<LateRequestModel> lateRequestOptional = lateRequestRepository.findById(requestId);
            if (lateRequestOptional.isEmpty()) {
                return new ResponseEntity<>("Late request not found", HttpStatus.NOT_FOUND);
            }

            LateRequestModel lateRequest = lateRequestOptional.get();


            Optional<UsersModel> userOptional = usersService.findByEmail(lateRequestDto.getEmail());
            if (userOptional.isEmpty()) {
                return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
            }

            UsersModel userModel = userOptional.get();
            if (!userModel.getUserId().equals(lateRequest.getUserId())) {
                return new ResponseEntity<>("User is not authorized to update this request", HttpStatus.FORBIDDEN);
            }

            lateRequest.setReason(lateRequestDto.getReason());
            lateRequest.setDate(lateRequestDto.getDate());
            lateRequest.setStatus(LateRequestStatus.PENDING);
            lateRequestRepository.save(lateRequest);
            LateRequestResponseDto responseDto = new LateRequestResponseDto(
                    lateRequest.getUserId(),
                    lateRequest.getName(),
                    userModel.getEmail(),
                    lateRequest.getBatch(),
                    lateRequest.getReason(),
                    lateRequest.getDate(),
                    lateRequest.getStatus().name()
            );

            return new ResponseEntity<>(responseDto, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/late-requests")
    public ResponseEntity<?> getAllLateRequestsForUser(@RequestParam Long userId) {
        try {
            List<LateRequestModel> lateRequests = lateRequestRepository.findByUserId(userId);
            List<LateRequestResponseDto> responseDtos = lateRequests.stream()
                    .map(request -> new LateRequestResponseDto(
                            request.getUserId(),
                            usersService.getUserById(userId).get().getName(),
                            usersService.getUserById(userId).get().getEmail(),
                            usersService.getUserById(userId).get().getBatch(),
                            request.getReason(),
                            request.getDate(),
                            request.getStatus().name()
                    ))
                    .collect(Collectors.toList());

            return new ResponseEntity<>(responseDtos, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
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
            e.printStackTrace();
        }
        return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
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

