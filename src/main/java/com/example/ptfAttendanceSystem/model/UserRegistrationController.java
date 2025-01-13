package com.example.ptfAttendanceSystem.model;


import com.example.ptfAttendanceSystem.attendance.Attendance;
import com.example.ptfAttendanceSystem.attendance.AttendanceRepository;
import com.example.ptfAttendanceSystem.late.*;
import com.example.ptfAttendanceSystem.leave.LeaveRequestDto;
import com.example.ptfAttendanceSystem.leave.LeaveRequestModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
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

                // Generate a token for the admin
                String token = UUID.randomUUID().toString();
                userModel.setToken(token); // Set the token in the admin model
                usersService.updateUserToken(userModel); // Save the token in the database
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


//    @PostMapping(path="/inScanQR")
//    public ResponseEntity<?> scanInAndOut(@RequestParam Long userId,@RequestBody InScanDto inScanDto){
//        try {
//            return usersService.scanInAndOut(userId,inScanDto);
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//        return new ResponseEntity<>("Something went wrong",HttpStatus.INTERNAL_SERVER_ERROR);
//    }

    @PostMapping(path = "/inScanQR")
    public ResponseEntity<?> scanInAndOut(@RequestParam Long userId, @RequestBody InScanDto inScanDto) {
        try {
            return usersService.scanInAndOut(userId, inScanDto);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get attendance for a specific user on a particular date
    @GetMapping("/attendance/{userId}")
    public ResponseEntity<?> getAttendance(@PathVariable Long userId, @RequestParam("date") String date) {
        LocalDate attendanceDate = LocalDate.parse(date);
        Optional<Attendance> attendance = attendanceRepository.findByUserIdAndAttendanceDate(userId, attendanceDate);

        if (attendance.isPresent()) {
            return new ResponseEntity<>(attendance.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Attendance not found for user on this date", HttpStatus.NOT_FOUND);
        }
    }

    // Get attendance for all users on a particular date
    @GetMapping("/attendance/date/{date}")
    public ResponseEntity<?> getAllUserAttendance(@PathVariable String date) {
        LocalDate attendanceDate = LocalDate.parse(date);
        List<Attendance> allAttendance = attendanceRepository.findByAttendanceDate(attendanceDate);

        if (allAttendance.isEmpty()) {
            return new ResponseEntity<>("No attendance records found for this date", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(allAttendance, HttpStatus.OK);
    }

    // New method to get attendance for all users on the current date
    @GetMapping("/attendance/today")
    public ResponseEntity<?> getAllUserAttendanceToday() {
        LocalDate currentDate = LocalDate.now();
        List<Attendance> allAttendance = attendanceRepository.findByAttendanceDate(currentDate);

        if (allAttendance.isEmpty()) {
            return new ResponseEntity<>("No attendance records found for today", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(allAttendance, HttpStatus.OK);
    }


    // New endpoint to get attendance for a user on a particular month
    @GetMapping("/attendance/month/{userId}")
    public ResponseEntity<?> getAttendanceForMonth(@PathVariable Long userId,
                                                   @RequestParam("month") int month,
                                                   @RequestParam("year") int year) {
        // Validate month (1 to 12)
        if (month < 1 || month > 12) {
            return new ResponseEntity<>("Invalid month", HttpStatus.BAD_REQUEST);
        }

        List<Attendance> attendanceList = usersService.getAttendanceForMonth(userId, month, year);

        if (attendanceList.isEmpty()) {
            return new ResponseEntity<>("No attendance records found for user in this month", HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(attendanceList, HttpStatus.OK);
    }

    @PostMapping("/leave-request/{userId}")
//    public ResponseEntity<?> requestLeave(@PathVariable Long userId, @RequestBody LeaveRequestDto leaveRequestDto) {
//        try {
//            return usersService.createLeaveRequest(leaveRequestDto, userId);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }

    public ResponseEntity<LeaveResponseDto> leaveRequestMethod(@PathVariable Long userId,@RequestBody LeaveRequestDto leaveRequestDto){
        try {
            return usersService.leaveRequestMethod(userId,leaveRequestDto);
        }catch (Exception e){
            e.printStackTrace();
        }
        return new ResponseEntity<>(null,HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @DeleteMapping("/leave-request/{leaveRequestId}")
    public ResponseEntity<String> deleteLeaveRequest(@PathVariable Long leaveRequestId) {
        try {
            boolean isDeleted = usersService.deleteLeaveRequest(leaveRequestId);
            if (isDeleted) {
                return new ResponseEntity<>("Leave request deleted successfully", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Leave request not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Error occurred while deleting leave request", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/leave-request/{leaveId}")
    public ResponseEntity<?> updateLeaveRequest(@PathVariable Long leaveId, @RequestBody LeaveRequestDto leaveRequestDto) {
        try {
            return usersService.updateLeaveRequest(leaveId, leaveRequestDto);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Unable to update leave request", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/leave-requests/{userId}")
    public ResponseEntity<?> getLeaveRequests(@PathVariable Long userId) {
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

    @PostMapping("/late-request/{userId}")
    public ResponseEntity<?> requestLate(@PathVariable Long userId, @RequestBody LateRequestDto lateRequestDto) {
        try {
            // Step 1: Find user by email
            Optional<UsersModel> user = usersService.findByEmail(lateRequestDto.getEmail());

            if (user.isEmpty()) {
                return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
            }

            UsersModel userModel = user.get();

            // Step 2: Check batch and the time constraint
            String batch = userModel.getBatch();
            LocalTime currentTime = LocalTime.now();
            LocalTime allowedTime = null;

            if ("morning batch".equalsIgnoreCase(batch)) {
                allowedTime = LocalTime.of(11, 40);  // Batch 1's time restriction is 9:30 AM
            } else if ("evening batch".equalsIgnoreCase(batch)) {
                allowedTime = LocalTime.of(23, 40); // Batch 2's time restriction is 1:30 PM (13:30)
            } else {
                return new ResponseEntity<>("Invalid batch", HttpStatus.BAD_REQUEST);
            }

            // Step 3: Check if the date is in the past
            LocalDate requestedDate = lateRequestDto.getDate();
            LocalDate currentDate = LocalDate.now();

            if (requestedDate.isBefore(currentDate)) {
                return new ResponseEntity<>("Late request cannot be made for a past date", HttpStatus.BAD_REQUEST);
            }

            // Step 4: Compare current time with the allowed time for batch
            if (currentTime.isAfter(allowedTime)) {
                String errorMessage = "Late request not allowed for this batch after " + allowedTime.toString();
                return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
            }

            // Step 5: Process the late request
            LateRequestModel lateRequest = new LateRequestModel();
            lateRequest.setUserId(userModel.getUserId());
            lateRequest.setReason(lateRequestDto.getReason());
            lateRequest.setDate(lateRequestDto.getDate());
            lateRequest.setStatus(LateRequestStatus.PENDING);  // Default status is PENDING
            lateRequest.setName(userModel.getName());
            lateRequest.setBatch(userModel.getBatch());

            // Save the late request in the repository
            lateRequestRepository.save(lateRequest);

            // Step 6: Prepare response DTO with status
            LateRequestResponseDto responseDto = new LateRequestResponseDto(
                    userModel.getUserId(),
                    userModel.getName(),
                    userModel.getEmail(),
                    userModel.getBatch(),
                    lateRequestDto.getReason(),
                    lateRequestDto.getDate(),
                    lateRequest.getStatus().name()  // Add the status field to the response
            );

            return new ResponseEntity<>(responseDto, HttpStatus.CREATED);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


//    @PostMapping("/late-request/{userId}")
//    public ResponseEntity<?> requestLate(@PathVariable Long userId,@RequestBody LateRequestDto lateRequestDto) {
//        try {
//            // Step 1: Find user by email
//            Optional<UsersModel> user = usersService.findByEmail(lateRequestDto.getEmail());
//
//            if (user.isEmpty()) {
//                return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
//            }
//
//            UsersModel userModel = user.get();
//
//            // Step 2: Check batch and the time constraint
//            String batch = userModel.getBatch();
//            LocalTime currentTime = LocalTime.now();
//            LocalTime allowedTime = null;
//
//            if ("morning batch".equalsIgnoreCase(batch)) {
//                allowedTime = LocalTime.of(11, 40);  // Batch 1's time restriction is 9:30 AM
//            } else if ("evening batch".equalsIgnoreCase(batch)) {
//                allowedTime = LocalTime.of(23, 40); // Batch 2's time restriction is 1:30 PM (13:30)
//            } else {
//                return new ResponseEntity<>("Invalid batch", HttpStatus.BAD_REQUEST);
//            }
//
//            // Step 3: Check if the date is in the past
//            LocalDate requestedDate = lateRequestDto.getDate();
//            LocalDate currentDate = LocalDate.now();
//
//            if (requestedDate.isBefore(currentDate)) {
//                return new ResponseEntity<>("Late request cannot be made for a past date", HttpStatus.BAD_REQUEST);
//            }
//
//
//            // Step 4: Compare current time with the allowed time for batch
//            if (currentTime.isAfter(allowedTime)) {
//                String errorMessage = "Late request not allowed for this batch after " + allowedTime.toString();
//                return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
//            }
//
//
//
//            // Step 5: Process the late request
//            LateRequestModel lateRequest = new LateRequestModel();
//            lateRequest.setUserId(userModel.getUserId());
//            lateRequest.setReason(lateRequestDto.getReason());
//            lateRequest.setDate(lateRequestDto.getDate());
//            lateRequest.setStatus(LateRequestStatus.PENDING);
//            lateRequest.setName(userModel.getName());
//            lateRequest.setBatch(userModel.getBatch());
//
//            // Save the late request in the repository
//            lateRequestRepository.save(lateRequest);
//
//            // Step 6: Prepare response DTO
//            LateRequestResponseDto responseDto = new LateRequestResponseDto(
//                    userModel.getUserId(),
//                    userModel.getName(),
//                    userModel.getEmail(),
//                    userModel.getBatch(),
//                    lateRequestDto.getReason(),
//                    lateRequestDto.getDate()
//            );
//
//            return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }


    @DeleteMapping("/late-request/{id}")
    public ResponseEntity<?> deleteLateRequest(@PathVariable Long id) {
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



    @PutMapping("/late-request/{requestId}")
    public ResponseEntity<?> updateLateRequest(@PathVariable Long requestId, @RequestBody LateRequestDto lateRequestDto) {
        try {
            // Step 1: Find the existing late request by its ID
            Optional<LateRequestModel> lateRequestOptional = lateRequestRepository.findById(requestId);
            if (lateRequestOptional.isEmpty()) {
                return new ResponseEntity<>("Late request not found", HttpStatus.NOT_FOUND);
            }

            LateRequestModel lateRequest = lateRequestOptional.get();

            // Step 2: Validate the user who is trying to update the request
            Optional<UsersModel> userOptional = usersService.findByEmail(lateRequestDto.getEmail());
            if (userOptional.isEmpty()) {
                return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
            }

            UsersModel userModel = userOptional.get();
            if (!userModel.getUserId().equals(lateRequest.getUserId())) {
                return new ResponseEntity<>("User is not authorized to update this request", HttpStatus.FORBIDDEN);
            }

            // Step 3: Update the late request details
            lateRequest.setReason(lateRequestDto.getReason());
            lateRequest.setDate(lateRequestDto.getDate());
            lateRequest.setStatus(LateRequestStatus.PENDING); // You can also add logic for changing the status if needed

            // Step 4: Save the updated late request in the repository
            lateRequestRepository.save(lateRequest);

            // Step 5: Prepare the response DTO with updated information
            LateRequestResponseDto responseDto = new LateRequestResponseDto(
                    lateRequest.getUserId(),
                    lateRequest.getName(),
                    userModel.getEmail(),
                    lateRequest.getBatch(),
                    lateRequest.getReason(),
                    lateRequest.getDate(),
                    lateRequest.getStatus().name()  // Updated status
            );

            return new ResponseEntity<>(responseDto, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


//    @PutMapping("/late-request/update/{id}")
//    public ResponseEntity<?> updateLateRequest(@PathVariable Long id, @RequestBody LateRequestDto lateRequestDto) {
//        try {
//            Optional<LateRequestModel> existingRequest = lateRequestRepository.findById(id);
//
//            if (existingRequest.isEmpty()) {
//                return new ResponseEntity<>("Late request not found", HttpStatus.NOT_FOUND);
//            }
//
//            LateRequestModel request = existingRequest.get();
//            request.setReason(lateRequestDto.getReason());
//            request.setDate(lateRequestDto.getDate());
//
//            lateRequestRepository.save(request);
//
//            return new ResponseEntity<>(request, HttpStatus.OK);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new ResponseEntity<>("Error updating late request", HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//    private LocalTime getAllowedTime(String batch) {
//        if ("morning batch".equalsIgnoreCase(batch)) {
//            return LocalTime.of(11, 40);
//        } else if ("evening batch".equalsIgnoreCase(batch)) {
//            return LocalTime.of(23, 40);
//        }
//        return null;
//    }
//
//    private LateRequestResponseDto buildResponseDto(UsersModel userModel, LateRequestDto lateRequestDto) {
//        return new LateRequestResponseDto(
//                userModel.getUserId(),
//                userModel.getName(),
//                userModel.getEmail(),
//                userModel.getBatch(),
//                lateRequestDto.getReason(),
//                lateRequestDto.getDate()
//        );
//    }



    @GetMapping("/late-requests/{userId}")
    public ResponseEntity<?> getAllLateRequestsForUser(@PathVariable Long userId) {
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
                            request.getStatus().name()  // Include status in the response DTO
                    ))
                    .collect(Collectors.toList());

            return new ResponseEntity<>(responseDtos, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

//    @GetMapping("/late-requests/{userId}")
//    public ResponseEntity<?> getAllLateRequestsForUser(@PathVariable Long userId) {
//        try {
//            List<LateRequestModel> lateRequests = lateRequestRepository.findByUserId(userId);
//            List<LateRequestResponseDto> responseDtos = lateRequests.stream()
//                    .map(request -> new LateRequestResponseDto(
//                            request.getUserId(),
//                            usersService.getUserById(userId).get().getName(),
//                            usersService.getUserById(userId).get().getEmail(),
//                            usersService.getUserById(userId).get().getBatch(),
//                            request.getReason(),
//                            request.getDate()))
//                    .collect(Collectors.toList());
//
//            return new ResponseEntity<>(responseDtos, HttpStatus.OK);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }


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

//    @GetMapping(path = "/get/{id}")
//    public ResponseEntity<?> getUserById(@PathVariable Long id) {
//        try {
//            Optional<UsersModel> user = usersService.getUserById(id);
//            if (user.isPresent()) {
//                return new ResponseEntity<>(user.get(), HttpStatus.OK);
//            } else {
//                return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//


    @PutMapping(path = "/updatePassword/{id}")
    public ResponseEntity<?> updateUserPassword(@PathVariable Long id, @RequestBody UserDto userDto) {
        try {
            UsersModel updatedUser = usersService.updateUserPassword(id, userDto);
            return new ResponseEntity<>(updatedUser, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    // New endpoint for deleting a user
    @DeleteMapping(path = "/delete/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
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

    // New endpoint for updating a user
    @PutMapping(path = "/update/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserDto userDto) {
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
            // Validate request data
            if (!forgotPasswordDto.getNewPassword().equals(forgotPasswordDto.getConfirmPassword())) {
                return new ResponseEntity<>("Passwords do not match", HttpStatus.BAD_REQUEST);
            }

            // Check if user exists
            Optional<UsersModel> userOptional = usersRepository.findByEmail(forgotPasswordDto.getEmail());
            if (userOptional.isEmpty()) {
                return new ResponseEntity<>("User not found with the provided email", HttpStatus.NOT_FOUND);
            }

            // Update user's password
            UsersModel user = userOptional.get();
            user.setPassword(forgotPasswordDto.getNewPassword()); // Ensure password is securely hashed
            usersRepository.save(user);

            return new ResponseEntity<>("Password updated successfully", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}

