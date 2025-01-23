package com.example.ptfAttendanceSystem.admin;


import com.example.ptfAttendanceSystem.attendance.Attendance;
import com.example.ptfAttendanceSystem.attendance.AttendanceRepository;
import com.example.ptfAttendanceSystem.late.LateRequestModel;
import com.example.ptfAttendanceSystem.late.LateRequestRepository;
import com.example.ptfAttendanceSystem.late.LateRequestStatus;
import com.example.ptfAttendanceSystem.leave.LeaveRequestModel;
import com.example.ptfAttendanceSystem.leave.LeaveRequestRepository;
import com.example.ptfAttendanceSystem.leave.LeaveRequestStatus;
import com.example.ptfAttendanceSystem.model.ForgotPasswordDto;
import com.example.ptfAttendanceSystem.model.UsersModel;
import com.example.ptfAttendanceSystem.model.UsersRepository;
import com.example.ptfAttendanceSystem.model.UsersService;
import com.example.ptfAttendanceSystem.qr.QRCodeService;
import com.example.ptfAttendanceSystem.qr.ScanDto;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
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

                // Generate a token for the admin
                String token = UUID.randomUUID().toString();
                adminModel.setToken(token); // Set the token in the admin model
                adminService.updateAdminToken(adminModel); // Save the token in the database

                ALoginResponseDto responseDto = new ALoginResponseDto(
                        adminModel.getId(),
                        adminModel.getEmail(),
                        adminModel.getName(),
                        adminModel.getToken(),
//                        token, // Include the token in the response
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
            // Validate request data
            if (!forgotPasswordDto.getNewPassword().equals(forgotPasswordDto.getConfirmPassword())) {
                return new ResponseEntity<>("Passwords do not match", HttpStatus.BAD_REQUEST);
            }

            // Check if user exists
            Optional<AdminModel> adminOptional = adminRepository.findByEmail(forgotPasswordDto.getEmail());
            if (adminOptional.isEmpty()) {
                return new ResponseEntity<>("Admin not found with the provided email", HttpStatus.NOT_FOUND);
            }

            // Update admin password
            AdminModel admin = adminOptional.get();
            admin.setPassword(forgotPasswordDto.getNewPassword()); // Ensure password is securely hashed
            adminRepository.save(admin);

            return new ResponseEntity<>("Password updated successfully", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

//    // Add method to generate QR code after scanning
//    @GetMapping(path = "/generateQr")
//    public ResponseEntity<?> generateQr(HttpServletResponse response) {
//        try {
//            String qrContent = UUID.randomUUID().toString(); // Generate unique content for each scan
//            generateQrCode(qrContent, response);
//            return new ResponseEntity<>("QR code generated successfully", HttpStatus.OK);
//        } catch (WriterException | IOException e) {
//            e.printStackTrace();
//            return new ResponseEntity<>("Error generating QR code", HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//    // Method to generate and write QR code to response
//    private void generateQrCode(String content, HttpServletResponse response) throws WriterException, IOException {
//        QRCodeWriter qrCodeWriter = new QRCodeWriter();
//        BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, 200, 200);
//
//        // Set response headers for the image output
//        response.setContentType("image/png");
//        OutputStream out = response.getOutputStream();
//        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", out);
//        out.flush();
//        out.close();
//    }


//17/12/24 working with frontend
//    // Add method to generate QR code with current date and time
//    @GetMapping(path = "/generateQr")
//    public ResponseEntity<?> generateQr() {
//        try {
//            // Get the current date and time
//            LocalDateTime now = LocalDateTime.now();
//            String formattedDate = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
//            String formattedTime = now.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
//
//            // Create content for the QR code
//            String qrContent = "date:" + formattedDate + "  time:" + formattedTime;
//
//            // Generate Base64 QR Code String
//            String base64QRCode = qrCodeService.generateQRCodeAsString(qrContent);
//
//            // Return the Base64 string as the response
//            return new ResponseEntity<>(base64QRCode, HttpStatus.OK);
//        } catch (WriterException | IOException e) {
//            e.printStackTrace();
//            return new ResponseEntity<>("Error generating QR code", HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }


//15/1/25

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





//
//    // Endpoint to retrieve the current QR code
//    @GetMapping(path = "/generateQr")
//    public ResponseEntity<?> getQRCode() {
//        try {
//            // Retrieve the pre-generated QR code
//            String currentQRCode = qrCodeService.getCurrentQRCode();
//            return new ResponseEntity<>(currentQRCode, HttpStatus.OK);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new ResponseEntity<>("Error retrieving QR code", HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }

    // Method to generate and write QR code to response
    private void generateQrCode(String content, HttpServletResponse response) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, 200, 200);

        // Set response headers for the image output
        response.setContentType("image/png");
        try (OutputStream out = response.getOutputStream()) {
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", out);
        }
    }

    @PostMapping(path = "/scan")
    public ResponseEntity<?> handleScan(@RequestBody ScanDto scanDto) {
        try {
            // Handle the scan logic here, such as saving to the database
            adminService.handleScan(scanDto.getToken());  // Use the token or user ID to identify the scan

            // Return a response indicating successful scan
            return new ResponseEntity<>("Scan recorded successfully", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Error handling scan", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }




//    @GetMapping(path = "/generateQr")
//    public ResponseEntity<?> getQRCode() {
//        try {
//            // Retrieve the pre-generated QR code
//            String currentQRCode = qrCodeService.getCurrentQRCode();
//            return new ResponseEntity<>(currentQRCode, HttpStatus.OK);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new ResponseEntity<>("Error retrieving QR code", HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//    // Method to generate and write QR code to response
//    private void generateQrCode(String content, HttpServletResponse response) throws WriterException, IOException {
//        QRCodeWriter qrCodeWriter = new QRCodeWriter();
//        BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, 200, 200);
//
//        // Set response headers for the image output
//        response.setContentType("image/png");
//        try (OutputStream out = response.getOutputStream()) {
//            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", out);
//        }
//    }
//
//    @PostMapping(path = "/scan")
//    public ResponseEntity<?> handleScan(@RequestBody ScanDto scanDto) {
//        try {
//            // Handle the scan logic here, such as saving to the database
//            adminService.handleScan(scanDto.getToken());  // Use the token or user ID to identify the scan
//
//            // Return a response indicating successful scan
//            return new ResponseEntity<>("Scan recorded successfully", HttpStatus.OK);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new ResponseEntity<>("Error handling scan", HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }

    @GetMapping("/getAllUsers")
    public ResponseEntity<List<UsersModel>> getAllUsers() {
        try {
            List<UsersModel> users = usersService.getAllUsers();
            return new ResponseEntity<>(users, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
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


    @GetMapping("/getLeaveRequestsForToday")
    public ResponseEntity<?> getLeaveRequestsForToday() {
        try {
            List<Map<String, Object>> leaveRequests = adminService.getLeaveRequestsForTodayWithUserDetails();

            if (leaveRequests.isEmpty()) {
                return new ResponseEntity<>("No leave requests for today", HttpStatus.OK);
            }

            return new ResponseEntity<>(leaveRequests, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }










//    // Fetch all leave requests for today
//    @GetMapping("/getLeaveRequestsForToday")
//    public ResponseEntity<?> getLeaveRequestsForToday() {
//        try {
//            List<LeaveRequestModel> leaveRequests = adminService.getLeaveRequestsForToday();
//
//            if (leaveRequests.isEmpty()) {
//                return new ResponseEntity<>("No leave requests for today", HttpStatus.OK);
//            }
//
//            return new ResponseEntity<>(leaveRequests, HttpStatus.OK);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }

    // Approve leave request
    @PostMapping("/approveLeaveRequest/{leaveRequestId}")
    public ResponseEntity<?> approveLeaveRequest(@PathVariable Long leaveRequestId) {
        try {
            return adminService.approveLeaveRequest(leaveRequestId);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Fetch leave requests based on status (PENDING, APPROVED, REJECTED)
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



    // Reject leave request
    @PostMapping("/rejectLeaveRequest/{leaveRequestId}")
    public ResponseEntity<?> rejectLeaveRequest(@PathVariable Long leaveRequestId) {
        try {
            return adminService.rejectLeaveRequest(leaveRequestId);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }




    // Fetch all late requests for today
    @GetMapping("/getLateRequestsForToday")
    public ResponseEntity<?> getLateRequestsForToday() {
        try {
            List<LateRequestModel> lateRequests = adminService.getLateRequestsForToday();

            if (lateRequests.isEmpty()) {
                return new ResponseEntity<>("No late requests for today", HttpStatus.OK);
            }

            return new ResponseEntity<>(lateRequests, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Fetch late requests based on status (PENDING, APPROVED, REJECTED)
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

    // Approve late request
    @PostMapping("/approveLateRequest/{lateRequestId}")
    public ResponseEntity<?> approveLateRequest(@PathVariable Long lateRequestId) {
        try {
            return adminService.approveLateRequest(lateRequestId);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Reject late request
    @PostMapping("/rejectLateRequest/{lateRequestId}")
    public ResponseEntity<?> rejectLateRequest(@PathVariable Long lateRequestId) {
        try {
            return adminService.rejectLateRequest(lateRequestId);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
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


    // New method to get attendance between two dates
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

    // New method to get a user's attendance for a particular month
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

    // New method to get a user's attendance between two specific dates
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

    // New endpoint for deleting a user
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

    // New endpoint to delete attendance for a given month
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


}
