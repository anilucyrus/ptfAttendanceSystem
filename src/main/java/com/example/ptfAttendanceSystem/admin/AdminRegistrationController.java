package com.example.ptfAttendanceSystem.admin;




import com.example.ptfAttendanceSystem.InAndOut.InAndOutRepository;
import com.example.ptfAttendanceSystem.InAndOut.InOut;
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
import com.example.ptfAttendanceSystem.leaveAndWFH.*;
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
import java.time.temporal.ChronoUnit;
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

    @Autowired
    private LeaveWfhRepo leaveWfhRepo;


    @Autowired
    private WfhService wfhService;

    @Autowired
    private InAndOutRepository inAndOutRepository;

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
                return new ResponseEntity<>("No details found", HttpStatus.UNAUTHORIZED);
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
                return new ResponseEntity<>("Admin not found with the provided email", HttpStatus.NO_CONTENT);
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
            @RequestParam(required = false) Long batchId) {

        if (batchId != null && !adminService.isBatchExists(batchId)) {
            return new ResponseEntity<>("Batch not found", HttpStatus.BAD_REQUEST);
        }

        List<Map<String, Object>> leaveRequests = adminService.getLeaveRequestsForTodayWithUserDetails(batchId);
        if (leaveRequests.isEmpty()) {
            return new  ResponseEntity<>("No leave requests for today", HttpStatus.NO_CONTENT);
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
    public ResponseEntity<?> getLeaveRequestsByStatus(
            @RequestParam LeaveRequestStatus status,
            @RequestParam(required = false) Long batchId) {

        try {
            List<LeaveRequestModel> leaveRequests = adminService.getLeaveRequestsByStatusAndBatch(status, batchId);

            if (batchId != null && !adminService.isBatchExists(batchId)) {
                return new ResponseEntity<>("Batch not found", HttpStatus.BAD_REQUEST);
            }

            if (leaveRequests.isEmpty()) {
                return new ResponseEntity<>("No leave requests found for the given status and batch", HttpStatus.NO_CONTENT);
            }

            List<Map<String, Object>> responseList = leaveRequests.stream().map(leaveRequest -> {
                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("id", leaveRequest.getId());
                responseMap.put("userId", leaveRequest.getUserId());
                responseMap.put("fromDate", leaveRequest.getFromDate());
                responseMap.put("toDate", leaveRequest.getToDate());
                responseMap.put("status", leaveRequest.getStatus());
                responseMap.put("reason", leaveRequest.getReason());

                // Fetch user details
                UsersModel user = usersRepository.findById(leaveRequest.getUserId()).orElse(null);
                responseMap.put("userName", user != null ? user.getName() : "Unknown");

                // Fetch batch details
                String batchName = batchService.getBatchById(leaveRequest.getBatchId())
                        .map(BatchModel::getBatchName)
                        .orElse("Unknown");
                responseMap.put("batchId", leaveRequest.getBatchId());
                responseMap.put("batchName", batchName);

                return responseMap;
            }).collect(Collectors.toList());

            return new ResponseEntity<>(responseList, HttpStatus.OK);
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

    @GetMapping("/getLateRequestsForToday")
    public ResponseEntity<?> getLateRequestsForToday(@RequestParam(required = false) Long batchId) {
        try {
            if (batchId != null && !adminService.isBatchExists(batchId)) {
                return new ResponseEntity<>("Batch not found", HttpStatus.BAD_REQUEST);
            }

            List<LateRequestResponseDto> lateRequests = adminService.getLateRequestsForToday(batchId);

            if (lateRequests.isEmpty()) {
                return new ResponseEntity<>("No late requests for today", HttpStatus.NO_CONTENT);
            }

            return new ResponseEntity<>(lateRequests, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/getLateRequestsByStatus")
    public ResponseEntity<?> getLateRequestsByStatus(
            @RequestParam LateRequestStatus status,
            @RequestParam Long batchId) {

        try {
            List<LateRequestModel> lateRequests = adminService.getLateRequestsByStatusAndBatch(status, batchId);

            if (batchId != null && !adminService.isBatchExists(batchId)) {
                return new ResponseEntity<>("Batch not found", HttpStatus.BAD_REQUEST);
            }
            if (lateRequests.isEmpty()) {
                return new ResponseEntity<>("No late requests found for the given status and batch", HttpStatus.NO_CONTENT);
            }

            List<Map<String, Object>> responseList = lateRequests.stream().map(lateRequest -> {
                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("id", lateRequest.getId());
                responseMap.put("userId", lateRequest.getUserId());
                responseMap.put("reason", lateRequest.getReason());
                responseMap.put("date", lateRequest.getDate());
                responseMap.put("status", lateRequest.getStatus());

                // Fetch user details
                UsersModel user = usersRepository.findById(lateRequest.getUserId()).orElse(null);
                responseMap.put("userName", user != null ? user.getName() : "Unknown");

                // Fetch batch details
                String batchName = batchService.getBatchById(lateRequest.getBatchId())
                        .map(BatchModel::getBatchName)
                        .orElse("Unknown");
                responseMap.put("batchId", lateRequest.getBatchId());
                responseMap.put("batchName", batchName);

                return responseMap;
            }).collect(Collectors.toList());

            return new ResponseEntity<>(responseList, HttpStatus.OK);
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


    @GetMapping("/attendance/user/{userId}/month/{month}")
    public ResponseEntity<?> getUserAttendanceForMonth(@PathVariable Long userId, @PathVariable String month) {
        try {
            LocalDate startOfMonth = LocalDate.parse(month + "-01");
            LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());

            List<Attendance> userAttendance = attendanceRepository.findByUserIdAndAttendanceDateBetween(userId, startOfMonth, endOfMonth);
            Optional<UsersModel> userOpt = usersRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
            }

            Long batchId = userOpt.get().getBatchId();
            if (userAttendance.isEmpty()) {
                return new ResponseEntity<>("No attendance records found for this user in the specified month", HttpStatus.NO_CONTENT);
            }



            List<Map<String, Object>> attendanceResponse = userAttendance.stream().map(attendance -> {
                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("id", attendance.getId());
                responseMap.put("userId", attendance.getUserId());
                responseMap.put("userName", attendance.getUserName());
                responseMap.put("batchId", batchId);
                responseMap.put("batchName", attendance.getBatchName());
                responseMap.put("attendanceDate", attendance.getAttendanceDate());
                responseMap.put("scanInTime", attendance.getScanInTime());
                responseMap.put("scanOutTime", attendance.getScanOutTime());
                responseMap.put("status", attendance.getStatus());
                return responseMap;
            }).collect(Collectors.toList());

            return new ResponseEntity<>(attendanceResponse, HttpStatus.OK);
        } catch (DateTimeParseException e) {
            return new ResponseEntity<>("Invalid month format. Please use yyyy-MM format.", HttpStatus.BAD_REQUEST);
        }
    }


    @GetMapping("/attendance/userDate-range")
    public ResponseEntity<?> getUserAttendanceBetweenDates(
            @RequestParam Long userId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);

            if (end.isBefore(start)) {
                return new ResponseEntity<>("End date must be after start date.", HttpStatus.BAD_REQUEST);
            }

            List<Attendance> userAttendance = attendanceRepository.findByUserIdAndAttendanceDateBetween(userId, start, end);

            if (userAttendance.isEmpty()) {
                return new ResponseEntity<>("No attendance records found for this user in the specified date range", HttpStatus.NO_CONTENT);
            }

            // Fetch batch ID for each attendance entry
            List<Map<String, Object>> response = userAttendance.stream().map(attendance -> {
                Map<String, Object> attendanceData = new HashMap<>();
                attendanceData.put("id", attendance.getId());
                attendanceData.put("userId", attendance.getUserId());
                attendanceData.put("userName", attendance.getUserName());
                attendanceData.put("batchName", attendance.getBatchName());
                attendanceData.put("attendanceDate", attendance.getAttendanceDate());
                attendanceData.put("scanInTime", attendance.getScanInTime());
                attendanceData.put("scanOutTime", attendance.getScanOutTime());
                attendanceData.put("status", attendance.getStatus());
                batchService.getBatchByName(attendance.getBatchName()).ifPresent(batch -> {
                    attendanceData.put("batchId", batch.getId());
                });


                return attendanceData;
            }).collect(Collectors.toList());

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (DateTimeParseException e) {
            return new ResponseEntity<>("Invalid date format. Please use yyyy-MM-dd format.", HttpStatus.BAD_REQUEST);
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
                return new ResponseEntity<>("No attendance records found for the given date range", HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(attendanceList, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Invalid date format. Please use yyyy-MM-dd", HttpStatus.BAD_REQUEST);
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
                return new ResponseEntity<>("No records found", HttpStatus.NO_CONTENT);
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

    @GetMapping("/LeaveWfh")
    public ResponseEntity<List<LeaveWfh>> getAllWfh() {
        List<LeaveWfh> wfhList = wfhService.getAllWfh();
        if (wfhList.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(wfhList);
        }
        return ResponseEntity.ok(wfhList);
    }

//    @GetMapping("/LeaveWfh")
//    public ResponseEntity<List<LeaveWfh>> getAllWfh() {
//        List<LeaveWfh> wfhList = wfhService.getAllWfh();
//        return ResponseEntity.ok(wfhList);
//    }



    @PostMapping("LeaveWfh")
    public ResponseEntity<?> addWfh(@RequestBody LeaveWfh leaveWfh) {
        try {
            if (leaveWfh.getName() == null || leaveWfh.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("WFH name cannot be empty.");
            }

            Optional<LeaveWfh> existingWfh = leaveWfhRepo.findByName(leaveWfh.getName().trim());
            if (existingWfh.isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("WFH name already exists.");
            }

            LeaveWfh savedWfh = wfhService.addWfh(leaveWfh);
            return new ResponseEntity<>(savedWfh, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while saving WFH: " + e.getMessage());
        }
    }


    @PutMapping("LeaveWfh/{id}")
    public ResponseEntity<?> updateWfh(@PathVariable Integer id, @RequestBody LeaveWfh leaveWfh) {
        try {
            LeaveWfh updatedWfh = wfhService.updateWfh(id, leaveWfh);
            return ResponseEntity.ok(updatedWfh);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }


    @DeleteMapping("LeaveWfh/{id}")
    public ResponseEntity<String> deleteWfh(@PathVariable Integer id) {
        try {
            wfhService.deleteWfh(id);
            return ResponseEntity.ok("WFH entry deleted successfully.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping("approveWorkFromHomeRequest/{wfhRequestId}")
    public ResponseEntity<?> approveWorkFromHomeRequest(@PathVariable Long wfhRequestId) {
        try {
            return adminService.approveWorkFromHomeRequest(wfhRequestId);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("rejectWorkFromHomeRequest/{wfhRequestId}")
    public ResponseEntity<?> rejectWorkFromHomeRequest(@PathVariable Long wfhRequestId) {
        try {
            return adminService.rejectWorkFromHomeRequest(wfhRequestId);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

//    @GetMapping("/getWfhRequestsByStatusAndBatch")
//    public ResponseEntity<?> getWfhRequestsByStatusAndBatch(
//            @RequestParam WfhStatus status,
//            @RequestParam Long batchId) {
//
//        try {
//            List<Wfh> wfhRequests = wfhService.getWfhRequestsByStatusAndBatch(status, batchId);
//
//
//            if (batchId != null && !adminService.isBatchExists(batchId)) {
//                return new ResponseEntity<>("Batch not found", HttpStatus.BAD_REQUEST);
//            }
//
//            if (wfhRequests.isEmpty()) {
//                return new ResponseEntity<>("No WFH requests found for the given status and batch", HttpStatus.NO_CONTENT);
//            }
//
//            return new ResponseEntity<>(wfhRequests, HttpStatus.OK);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }


    @GetMapping("getWfhRequestsByStatusAndBatch")
    public ResponseEntity<?> getWfhRequestsByStatusAndBatch(
            @RequestParam WfhStatus status,
            @RequestParam Long batchId) {

        try {
            if (batchId != null && !adminService.isBatchExists(batchId)) {
                return new ResponseEntity<>("Batch not found", HttpStatus.BAD_REQUEST);
            }

            List<Wfh> wfhRequests = wfhService.getWfhRequestsByStatusAndBatch(status, batchId);

            if (wfhRequests.isEmpty()) {
                return new ResponseEntity<>("No WFH requests found for the given status and batch", HttpStatus.NO_CONTENT);
            }

            List<Map<String, Object>> responseList = wfhRequests.stream().map(wfh -> {
                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("id", wfh.getId());
                responseMap.put("userId", wfh.getUserId());
                responseMap.put("fromDate", wfh.getFromDate());
                responseMap.put("toDate", wfh.getToDate());
                responseMap.put("status", wfh.getStatus());
                responseMap.put("reason", wfh.getReason());
                responseMap.put("leaveType", wfh.getLeaveType());


                long numberOfDays = ChronoUnit.DAYS.between(wfh.getFromDate(), wfh.getToDate()) + 1;
                responseMap.put("numberOfDays", numberOfDays);


                UsersModel user = usersRepository.findById(wfh.getUserId()).orElse(null);
                responseMap.put("userName", user != null ? user.getName() : "Unknown");


                String batchName = batchService.getBatchById(batchId)
                        .map(BatchModel::getBatchName)
                        .orElse("Unknown");
                responseMap.put("batchId", batchId);
                responseMap.put("batchName", batchName);

                return responseMap;
            }).collect(Collectors.toList());

            return new ResponseEntity<>(responseList, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("attendance/user/{userId}/month/{month}/count")
    public ResponseEntity<?> getUserAttendanceCountForMonth(
            @PathVariable Long userId,
            @PathVariable String month,
            @RequestParam Long batchId) {
        try {
            LocalDate startOfMonth = LocalDate.parse(month + "-01");
            LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());
            Optional<UsersModel> userOpt = usersRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
            }

            UsersModel user = userOpt.get();
            Optional<BatchModel> batchOpt = batchService.getBatchById(batchId);
            if (batchOpt.isEmpty()) {
                return new ResponseEntity<>("Batch not found", HttpStatus.NOT_FOUND);
            }

            BatchModel batch = batchOpt.get();

            if (!user.getBatchId().equals(batchId)) {
                return new ResponseEntity<>("User does not belong to the specified batch", HttpStatus.BAD_REQUEST);
            }

            int attendanceCount = attendanceRepository.countByUserIdAndAttendanceDateBetween(userId, startOfMonth, endOfMonth);

            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("userName", user.getName());
            response.put("batchId", batchId);
            response.put("batchName", batch.getBatchName());
            response.put("month", month);
            response.put("attendanceCount", attendanceCount);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (DateTimeParseException e) {
            return new ResponseEntity<>("Invalid month format. Please use yyyy-MM.", HttpStatus.BAD_REQUEST);
        }
    }


    @GetMapping("breakScanToday")
    public ResponseEntity<?> getTodayBreakScans(@RequestParam Long userId) {
        LocalDate currentDate = LocalDate.now();
        List<InOut> breakRecords = inAndOutRepository.findByUserIdAndAttendanceDate(userId, currentDate);

        if (breakRecords.isEmpty()) {
            return new ResponseEntity<>("No break scans found for today", HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(breakRecords, HttpStatus.OK);
    }


    @GetMapping("inout/breakScanDate-range")
    public ResponseEntity<?> getUserInOutBetweenDates(
            @RequestParam Long userId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);

            if (end.isBefore(start)) {
                return new ResponseEntity<>("End date must be after start date.", HttpStatus.BAD_REQUEST);
            }

            List<InOut> userInOutRecords = inAndOutRepository.findByUserIdAndAttendanceDateBetween(userId, start, end);

            if (userInOutRecords.isEmpty()) {
                return new ResponseEntity<>("No In-Out records found for this user in the specified date range", HttpStatus.NO_CONTENT);
            }

            List<Map<String, Object>> response = userInOutRecords.stream().map(inOut -> {
                Map<String, Object> inOutData = new HashMap<>();
                inOutData.put("id", inOut.getId());
                inOutData.put("userId", inOut.getUserId());
                inOutData.put("userName", inOut.getUserName());
                inOutData.put("batchId", inOut.getBatchId());
                inOutData.put("batchName", inOut.getBatchName());
                inOutData.put("attendanceDate", inOut.getAttendanceDate());
                inOutData.put("scanInTime1", inOut.getScanInTime1());
                inOutData.put("scanOutTime1", inOut.getScanOutTime1());
                inOutData.put("scanInTime2", inOut.getScanInTime2());
                inOutData.put("scanOutTime2", inOut.getScanOutTime2());
                inOutData.put("scanInTime3", inOut.getScanInTime3());
                inOutData.put("scanOutTime3", inOut.getScanOutTime3());
                return inOutData;
            }).collect(Collectors.toList());

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (DateTimeParseException e) {
            return new ResponseEntity<>("Invalid date format. Please use yyyy-MM-dd format.", HttpStatus.BAD_REQUEST);
        }
    }

}
