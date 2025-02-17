package com.example.ptfAttendanceSystem.admin;




import com.example.ptfAttendanceSystem.attendance.Attendance;
import com.example.ptfAttendanceSystem.attendance.AttendanceRepository;
import com.example.ptfAttendanceSystem.batch.BatchModel;
import com.example.ptfAttendanceSystem.batch.BatchService;
import com.example.ptfAttendanceSystem.batchType.BatchTypeModel;
import com.example.ptfAttendanceSystem.batchType.BatchTypeService;
import com.example.ptfAttendanceSystem.late.LateRequestModel;
import com.example.ptfAttendanceSystem.late.LateRequestRepository;
import com.example.ptfAttendanceSystem.late.LateRequestResponseDto;
import com.example.ptfAttendanceSystem.late.LateRequestStatus;
import com.example.ptfAttendanceSystem.leave.LeaveRequestModel;
import com.example.ptfAttendanceSystem.leave.LeaveRequestRepository;
import com.example.ptfAttendanceSystem.leave.LeaveRequestStatus;
import com.example.ptfAttendanceSystem.model.*;
import com.example.ptfAttendanceSystem.qr.QRCodeService;
import com.example.ptfAttendanceSystem.qr.ScanDto;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@CrossOrigin
@RequestMapping(path = "/AdminReg")
public class AdminRegistrationController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private UsersService usersService;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private QRCodeService qrCodeService;

    @Autowired
    private AttendanceRepository attendanceRepository;


    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private LateRequestRepository lateRequestRepository;

    @Autowired
    private BatchService batchService;

    @Autowired
    private BatchTypeService batchTypeService;

    @PostMapping(path = "/reg")
    public ResponseEntity<?> registration(@RequestBody AdminDto adminDto) {
        try {
            return adminService.adminRegistration(adminDto);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @PostMapping(path = "/login")
    public ResponseEntity<?> loginAdmin(@RequestBody ALoginDto aLoginDto) {
        try {
            Optional<AdminModel> adminOpt = adminService.findByEmailAndPassword(aLoginDto.getEmail(), aLoginDto.getPassword());
            if (adminOpt.isPresent()) {
                AdminModel adminModel = adminOpt.get();

                String token = UUID.randomUUID().toString();
                adminModel.setToken(token);
                adminService.updateAdminToken(adminModel);

                ALoginResponseDto responseDto = new ALoginResponseDto(
                        adminModel.getId(),
                        adminModel.getEmail(),
                        adminModel.getName(),
                        adminModel.getToken(),

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

    @PutMapping("/updateAdmin/{id}")
    public ResponseEntity<?> updateAdmin(@PathVariable int id, @RequestBody AdminDto adminDto) {
        try {
            return adminService.updateAdmin(id, adminDto);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Error updating admin", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @DeleteMapping(path = "/delete/{id}")
    public ResponseEntity<?> deleteAdmin(@PathVariable Long id) {
        try {
            boolean isDeleted = adminService.deleteAdmin(id);
            if (isDeleted) {
                return new ResponseEntity<>("User deleted successfully", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PutMapping(path = "/updatePassword/{id}")
    public ResponseEntity<?> updateUserPassword(@PathVariable Long id, @RequestBody AdminDto adminDto) {
        try {
            AdminModel updatedAdmin = adminService.updateAdminPassword(id, adminDto);
            return new ResponseEntity<>(updatedAdmin, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(path = "/forgot-password/{id}")
    public ResponseEntity<?> forgotPassword(@PathVariable Long id,@RequestBody ForgotPasswordDto forgotPasswordDto) {
        try {

            if (!forgotPasswordDto.getNewPassword().equals(forgotPasswordDto.getConfirmPassword())) {
                return new ResponseEntity<>("Passwords do not match", HttpStatus.BAD_REQUEST);
            }


            Optional<AdminModel> adminOptional = adminRepository.findByEmail(forgotPasswordDto.getEmail());
            if (adminOptional.isEmpty()) {
                return new ResponseEntity<>("Admin not found with the provided email", HttpStatus.NOT_FOUND);
            }


            AdminModel admin = adminOptional.get();
            admin.setPassword(forgotPasswordDto.getNewPassword());
            adminRepository.save(admin);

            return new ResponseEntity<>("Password updated successfully", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    @GetMapping(path = "/generateQr")
    public ResponseEntity<?> getQRCode() {
        try {
            String currentQRCode = qrCodeService.getCurrentQRCode();
            return new ResponseEntity<>(currentQRCode, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Error retrieving QR code", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(path = "/getQrStatus")
    public ResponseEntity<?> getQRCodeStatus() {
        try {
            int statusFlag = qrCodeService.getStatusFlag();
            return new ResponseEntity<>(Map.of("status", statusFlag), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(Map.of("error", "Error retrieving QR status"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    private void generateQrCode(String content, HttpServletResponse response) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, 200, 200);
        response.setContentType("image/png");
        try (OutputStream out = response.getOutputStream()) {
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", out);
        }
    }

    @PostMapping(path = "/scan")
    public ResponseEntity<?> handleScan(@RequestBody ScanDto scanDto) {
        try {
            adminService.handleScan(scanDto.getToken());
            return new ResponseEntity<>("Scan recorded successfully", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Error handling scan", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }




//    @GetMapping("/getAllUsers")
//    public ResponseEntity<List<UsersModel>> getAllUsers() {
//        try {
//            List<UsersModel> users = usersService.getAllUsers();
//            return new ResponseEntity<>(users, HttpStatus.OK);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }


    @GetMapping("/getAllUsers")
    public ResponseEntity<List<GetAllUsersDTO>> getAllUsers() {
        try {
            List<GetAllUsersDTO> users = usersService.getAllUsers();
            return new ResponseEntity<>(users, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(Collections.emptyList(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/get/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            Optional<UsersModel> user = usersService.getUserById(id);
            if (user.isPresent()) {
                return new ResponseEntity<>(user.get(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/leave-requests/today")
    public ResponseEntity<?> getLeaveRequestsForToday(
            @RequestParam(required = false) Long batchId) {  // batchId is optional

        if (batchId != null && !adminService.isBatchExists(batchId)) {
            return new ResponseEntity<>("Batch not found", HttpStatus.BAD_REQUEST);
        }

        List<Map<String, Object>> leaveRequests = adminService.getLeaveRequestsForTodayWithUserDetails(batchId);
        if (leaveRequests.isEmpty()) {
            return ResponseEntity.ok("No leave requests for today");
        }
        return ResponseEntity.ok(leaveRequests);
    }


    @PostMapping("/approveLeaveRequest/{leaveRequestId}")
    public ResponseEntity<?> approveLeaveRequest(@PathVariable Long leaveRequestId) {
        try {
            return adminService.approveLeaveRequest(leaveRequestId);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/getLeaveRequestsByStatus")
    public ResponseEntity<?> getLeaveRequestsByStatus(@RequestParam LeaveRequestStatus status) {
        try {
            List<LeaveRequestModel> leaveRequests = adminService.getLeaveRequestsByStatus(status);

            if (leaveRequests.isEmpty()) {
                return new ResponseEntity<>("No leave requests found for the given status", HttpStatus.OK);
            }

            return new ResponseEntity<>(leaveRequests, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/rejectLeaveRequest/{leaveRequestId}")
    public ResponseEntity<?> rejectLeaveRequest(@PathVariable Long leaveRequestId) {
        try {
            return adminService.rejectLeaveRequest(leaveRequestId);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

//    @GetMapping("/getLateRequestsForToday")
//    public ResponseEntity<?> getLateRequestsForToday() {
//        try {
//            List<LateRequestModel> lateRequests = adminService.getLateRequestsForToday();
//
//            if (lateRequests.isEmpty()) {
//                return new ResponseEntity<>("No late requests for today", HttpStatus.OK);
//            }
//
//            return new ResponseEntity<>(lateRequests, HttpStatus.OK);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }


//    @GetMapping("/getLateRequestsForToday")
//    public ResponseEntity<?> getLateRequestsForToday(@RequestParam(required = false) Long batchId) {
//        try {
//            List<LateRequestModel> lateRequests = adminService.getLateRequestsForToday(batchId);
//
//            if (lateRequests.isEmpty()) {
//                return new ResponseEntity<>("No late requests for today", HttpStatus.OK);
//            }
//
//            return new ResponseEntity<>(lateRequests, HttpStatus.OK);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }

    @GetMapping("/getLateRequestsForToday")
    public ResponseEntity<?> getLateRequestsForToday(@RequestParam(required = false) Long batchId) {
        try {
            if (batchId != null && !adminService.isBatchExists(batchId)) {
                return new ResponseEntity<>("Batch not found", HttpStatus.BAD_REQUEST);
            }

            List<LateRequestResponseDto> lateRequests = adminService.getLateRequestsForToday(batchId);

            if (lateRequests.isEmpty()) {
                return new ResponseEntity<>("No late requests for today", HttpStatus.OK);
            }

            return new ResponseEntity<>(lateRequests, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/getLateRequestsByStatus")
    public ResponseEntity<?> getLateRequestsByStatus(@RequestParam LateRequestStatus status) {
        try {
            List<LateRequestModel> lateRequests = adminService.getLateRequestsByStatus(status);

            if (lateRequests.isEmpty()) {
                return new ResponseEntity<>("No late requests found for the given status", HttpStatus.OK);
            }

            return new ResponseEntity<>(lateRequests, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping("/approveLateRequest/{lateRequestId}")
    public ResponseEntity<?> approveLateRequest(@PathVariable Long lateRequestId) {
        try {
            return adminService.approveLateRequest(lateRequestId);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping("/rejectLateRequest/{lateRequestId}")
    public ResponseEntity<?> rejectLateRequest(@PathVariable Long lateRequestId) {
        try {
            return adminService.rejectLateRequest(lateRequestId);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/attendance/today")
    public ResponseEntity<?> getAllUserAttendanceToday(@RequestParam Long batchId) {
        LocalDate currentDate = LocalDate.now();

        if (batchId != null && !adminService.isBatchExists(batchId)) {
            return new ResponseEntity<>("Batch not found", HttpStatus.BAD_REQUEST);
        }
        List<Attendance> allAttendance = attendanceRepository.findByAttendanceDate(currentDate);

        // Filter attendance records by batch ID
        List<Attendance> filteredAttendance = allAttendance.stream()
                .filter(attendance -> {
                    Optional<UsersModel> user = usersRepository.findById(attendance.getUserId());
                    return user.isPresent() && user.get().getBatchId().equals(batchId);
                })
                .collect(Collectors.toList());

        if (filteredAttendance.isEmpty()) {
            return new ResponseEntity<>("No attendance records found for today", HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(filteredAttendance, HttpStatus.OK);
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



    @GetMapping("/date-range")
    public ResponseEntity<?> getAttendanceBetweenDates(@RequestParam String startDate, @RequestParam String endDate) {
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);

            if (start.isAfter(end)) {
                return new ResponseEntity<>("Start date cannot be after end date", HttpStatus.BAD_REQUEST);
            }

            List<Attendance> attendanceList = attendanceRepository.findByAttendanceDateBetween(start, end);

            if (attendanceList.isEmpty()) {
                return new ResponseEntity<>("No attendance records found for the given date range", HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(attendanceList, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Invalid date format. Please use yyyy-MM-dd", HttpStatus.BAD_REQUEST);
        }
    }


    @GetMapping("/attendance/user/{userId}/month/{month}")
    public ResponseEntity<?> getUserAttendanceForMonth(@PathVariable Long userId, @PathVariable String month) {
        try {
            // Parse the month from the path (e.g., "2025-01")
            LocalDate startOfMonth = LocalDate.parse(month + "-01"); // Start of the month
            LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth()); // End of the month

            List<Attendance> userAttendance = attendanceRepository.findByUserIdAndAttendanceDateBetween(userId, startOfMonth, endOfMonth);

            if (userAttendance.isEmpty()) {
                return new ResponseEntity<>("No attendance records found for this user in the specified month", HttpStatus.NOT_FOUND);
            }

            return new ResponseEntity<>(userAttendance, HttpStatus.OK);
        } catch (DateTimeParseException e) {
            return new ResponseEntity<>("Invalid month format. Please use yyyy-MM format.", HttpStatus.BAD_REQUEST);
        }
    }


    @GetMapping("/attendance/userDate-range")
    public ResponseEntity<?> getUserAttendanceBetweenDates(@RequestParam Long userId, @RequestParam String startDate, @RequestParam String endDate) {
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);

            if (end.isBefore(start)) {
                return new ResponseEntity<>("End date must be after start date.", HttpStatus.BAD_REQUEST);
            }

            List<Attendance> userAttendance = attendanceRepository.findByUserIdAndAttendanceDateBetween(userId, start, end);

            if (userAttendance.isEmpty()) {
                return new ResponseEntity<>("No attendance records found for this user in the specified date range", HttpStatus.NOT_FOUND);
            }

            return new ResponseEntity<>(userAttendance, HttpStatus.OK);
        } catch (DateTimeParseException e) {
            return new ResponseEntity<>("Invalid date format. Please use yyyy-MM-dd format.", HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping(path = "/deleteUser/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        try {
            boolean isDeleted = usersService.deleteUser(userId);
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


    @DeleteMapping(path = "/deleteAttendance/{year}/{month}")
    public ResponseEntity<?> deleteAttendanceForMonth(@PathVariable int year, @PathVariable int month) {
        try {
            return adminService.deleteAttendanceForMonth(year, month);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>("Failed to delete attendance records", HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @DeleteMapping(path = "/deleteLeaveRequests/{year}/{month}")
    public ResponseEntity<?> deleteLeaveRequestsForMonth(@PathVariable int year, @PathVariable int month) {
        try {
            return adminService.deleteLeaveRequestsForMonth(year, month);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @DeleteMapping(path = "/deleteLateRequests/{year}/{month}")
    public ResponseEntity<?> deleteLateRequestsByMonth(@PathVariable int year, @PathVariable int month) {
        try {
            LocalDate startOfMonth = LocalDate.of(year, month, 1);
            LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());

            List<LateRequestModel> lateRequests = lateRequestRepository.findByDateBetween(startOfMonth, endOfMonth);

            if (lateRequests.isEmpty()) {
                return new ResponseEntity<>("No records found", HttpStatus.NOT_FOUND);
            }

            lateRequestRepository.deleteAll(lateRequests);
            return new ResponseEntity<>("Records deleted successfully", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping(path = "/addBatch")
    public ResponseEntity<?> addBatch(@RequestBody BatchModel batch, @RequestParam Long batchTypeId) {
        try {
            BatchModel createdBatch = batchService.addBatch(batch, batchTypeId);
            return new ResponseEntity<>(createdBatch, HttpStatus.CREATED);
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getReason());
        }
    }

    @PutMapping(path = "/updateBatch")
    public ResponseEntity<?> updateBatch(@RequestParam Long id, @RequestBody BatchModel batch, @RequestParam Long batchTypeId) {
        try {
            return new ResponseEntity<>(batchService.updateBatch(id, batch, batchTypeId), HttpStatus.OK);
        } catch (ResponseStatusException ex) {
            return new ResponseEntity<>(ex.getReason(), ex.getStatusCode());
        }
    }

    @DeleteMapping(path = "/deleteBatch")
    public ResponseEntity<?> deleteBatch(@RequestParam Long id) {
        try {
            batchService.deleteBatch(id);
            return new ResponseEntity<>("Batch deleted successfully", HttpStatus.OK);
        } catch (ResponseStatusException ex) {
            return new ResponseEntity<>(ex.getReason(), ex.getStatusCode());
        }
    }

    @GetMapping(path = "/getAllBatches")
    public ResponseEntity<?> getAllBatches() {
        try {
            return new ResponseEntity<>(batchService.getAllBatches(), HttpStatus.OK);
        } catch (ResponseStatusException ex) {
            return new ResponseEntity<>(ex.getReason(), ex.getStatusCode());
        }
    }




    @PostMapping(path = "/addBatchType")
    public ResponseEntity<?> addBatchType(@RequestBody BatchTypeModel batch_type) {
        return new ResponseEntity<>(batchTypeService.addBatchType(batch_type), HttpStatus.CREATED);
    }

    @DeleteMapping(path = "/deleteBatchType")
    public ResponseEntity<?> deleteBatchType(@RequestParam Long id) {
        try {
            batchTypeService.deleteBatchType(id);
            return new ResponseEntity<>("Batch Type deleted successfully", HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
    @GetMapping(path = "/getAllBatchType")
    public ResponseEntity<?> getAllBatchType() {
        return batchTypeService.getAllBatchType();
    }



    @PutMapping(path = "/updateBatchType")
    public ResponseEntity<?> updateBatchType(@RequestBody BatchTypeModel batchType) {
        if (batchType.getId() == null) {
            return new ResponseEntity<>("Batch Type ID is required for update", HttpStatus.BAD_REQUEST);
        }
        try {
            return new ResponseEntity<>(batchTypeService.updateBatchType(batchType), HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }




    @PutMapping(path = "/updateUser")
    public ResponseEntity<?> updateUserByAdmin(@RequestParam Long userId, @RequestBody UpdateUserDto updateUserDto) {
        return usersService.updateUser(userId, updateUserDto);
    }



}
