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
import com.example.ptfAttendanceSystem.qr.QRCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class AdminService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private LateRequestRepository lateRequestRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private AttendanceRepository attendanceRepository;
    @Autowired
    private QRCodeService qrCodeService;



    @Autowired



    public ResponseEntity<?> adminRegistration(AdminDto adminDto) {
        Optional<AdminModel> existingAdmin = adminRepository.findByEmail(adminDto.getEmail()); // Use the instance variable
        if (existingAdmin.isPresent()) {
            return new ResponseEntity<>("User already registered", HttpStatus.CONFLICT);
        }

        AdminModel admin = new AdminModel();
        admin.setName(adminDto.getName());
        admin.setEmail(adminDto.getEmail());
        admin.setPassword(adminDto.getPassword());
        AdminModel savedAdmin = adminRepository.save(admin);
        ARegistrationResponce responce = new ARegistrationResponce(
                savedAdmin.getId(),
                savedAdmin.getName(),
                savedAdmin.getEmail()
        );

        return new ResponseEntity<>(responce, HttpStatus.CREATED);
    }



    public void handleScan(String userId) {

        String newToken = UUID.randomUUID().toString();
        qrCodeService.setStatusFlag(1);
        System.out.println("New Token Generated: " + newToken);
    }
//
//    public List<Map<String, Object>> getLeaveRequestsForTodayWithUserDetails() {
//        LocalDate currentDate = LocalDate.now();
//        List<LeaveRequestModel> leaveRequests = leaveRequestRepository.findByFromDate(currentDate);
//
//        List<Map<String, Object>> leaveRequestList = new ArrayList<>();
//        for (LeaveRequestModel leaveRequest : leaveRequests) {
//            usersRepository.findById(leaveRequest.getUserId()).ifPresent(user -> {
//                Map<String, Object> response = new HashMap<>();
//                response.put("id", leaveRequest.getId());
//                response.put("userId", leaveRequest.getUserId());
//                response.put("leaveType", leaveRequest.getLeaveType());
//                response.put("reason", leaveRequest.getReason());
//                response.put("fromDate", leaveRequest.getFromDate());
//                response.put("toDate", leaveRequest.getToDate());
//                response.put("name", user.getName());
//                response.put("batchId", user.getBatchId());
//                response.put("numberOfDays", leaveRequest.getNumberOfDays());
//                response.put("status", leaveRequest.getStatus());
//                leaveRequestList.add(response);
//            });
//        }
//        return leaveRequestList;
//    }




    // Updated method with batchId filtering
    public List<Map<String, Object>> getLeaveRequestsForTodayWithUserDetails(Long batchId) {
        LocalDate currentDate = LocalDate.now();

        // Fetch leave requests for today
        List<LeaveRequestModel> leaveRequests;

        if (batchId != null) {
            // If batchId is provided, filter leave requests by batchId
            leaveRequests = leaveRequestRepository.findByFromDateAndBatchId(currentDate, batchId);
        } else {
            // If no batchId is provided, get all leave requests for today
            leaveRequests = leaveRequestRepository.findByFromDate(currentDate);
        }

        List<Map<String, Object>> leaveRequestList = new ArrayList<>();
        for (LeaveRequestModel leaveRequest : leaveRequests) {
            usersRepository.findById(leaveRequest.getUserId()).ifPresent(user -> {
                Map<String, Object> response = new HashMap<>();
                response.put("id", leaveRequest.getId());
                response.put("userId", leaveRequest.getUserId());
                response.put("leaveType", leaveRequest.getLeaveType());
                response.put("reason", leaveRequest.getReason());
                response.put("fromDate", leaveRequest.getFromDate());
                response.put("toDate", leaveRequest.getToDate());
                response.put("name", user.getName());
                response.put("batchId", user.getBatchId());
                response.put("numberOfDays", leaveRequest.getNumberOfDays());
                response.put("status", leaveRequest.getStatus());
                leaveRequestList.add(response);
            });
        }
        return leaveRequestList;
    }


    public List<LeaveRequestModel> getLeaveRequestsByStatus(LeaveRequestStatus status) {
        return leaveRequestRepository.findByStatus(status);
    }

    public ResponseEntity<?> approveLeaveRequest(Long leaveRequestId) {
        Optional<LeaveRequestModel> leaveRequestOptional = leaveRequestRepository.findById(leaveRequestId);
        if (leaveRequestOptional.isPresent()) {
            LeaveRequestModel leaveRequest = leaveRequestOptional.get();

            if (leaveRequest.getStatus() == LeaveRequestStatus.PENDING) {
                leaveRequest.setStatus(LeaveRequestStatus.APPROVED);
                leaveRequestRepository.save(leaveRequest);
                Optional<UsersModel> userOptional = usersRepository.findById(leaveRequest.getUserId());
                if (userOptional.isPresent()) {
                    UsersModel user = userOptional.get();
                    sendLeaveRequestApprovalEmail(user.getEmail(), leaveRequest);
                }

                return new ResponseEntity<>("Leave request approved", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Leave request is already processed", HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity<>("Leave request not found for ID: " + leaveRequestId, HttpStatus.NOT_FOUND);
        }
    }

    private void handleAttendanceForLeave(LeaveRequestModel leaveRequest) {
        LocalDate startDate = leaveRequest.getFromDate();
        LocalDate endDate = leaveRequest.getToDate();
        Long userId = leaveRequest.getUserId();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            Optional<Attendance> existingAttendance = attendanceRepository.findByUserIdAndAttendanceDate(userId, date);
            if (!existingAttendance.isPresent()) {
                Attendance attendance = new Attendance();
                attendance.setUserId(userId);
                attendance.setAttendanceDate(date);
                attendance.setStatus("Present");
                attendanceRepository.save(attendance);
            }
        }
    }

    private void sendLeaveRequestApprovalEmail(String userEmail, LeaveRequestModel leaveRequest) {
        String subject = "Leave Request Approved";
        String text = String.format(
                "Dear User,\n\n" +
                        "Good news! Your leave request for %s has been approved.\n" +
                        "The leave period is from %s to %s. \n" +
                        "You are granted a total of %d days of leave.\n\n" +
                        "Thank you.\n\n" +
                        "PTF Team.",
                leaveRequest.getLeaveType(),
                leaveRequest.getFromDate(),
                leaveRequest.getToDate(),
                leaveRequest.getNumberOfDays()
        );

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(userEmail);
        message.setSubject(subject);
        message.setText(text);

        try {
            mailSender.send(message);
        } catch (MailException e) {
            e.printStackTrace();

        }
    }


    public ResponseEntity<?> rejectLeaveRequest(Long leaveRequestId) {
        Optional<LeaveRequestModel> leaveRequestOptional = leaveRequestRepository.findById(leaveRequestId);

        if (leaveRequestOptional.isPresent()) {
            LeaveRequestModel leaveRequest = leaveRequestOptional.get();

            if (leaveRequest.getStatus() == LeaveRequestStatus.PENDING) {
                leaveRequest.setStatus(LeaveRequestStatus.REJECTED);
                leaveRequestRepository.save(leaveRequest);
                return new ResponseEntity<>("Leave request rejected", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Leave request is already processed", HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity<>("Leave request not found", HttpStatus.NOT_FOUND);
        }
    }



    public List<LateRequestModel> getLateRequestsForToday(Long batchId) {
        LocalDate currentDate = LocalDate.now();

        // If batchId is null, fetch all late requests for the current date.
        if (batchId != null) {
            return lateRequestRepository.findByDateAndBatchId(currentDate, batchId);
        } else {
            return lateRequestRepository.findByDate(currentDate);
        }
    }



//
//
//    public List<LateRequestModel> getLateRequestsForToday() {
//        LocalDate currentDate = LocalDate.now();
//        return lateRequestRepository.Date(currentDate);
//    }


    public List<LateRequestModel> getLateRequestsByStatus(LateRequestStatus status) {
        return lateRequestRepository.findByStatus(status);
    }

    public ResponseEntity<?> approveLateRequest(Long lateRequestId) {
        Optional<LateRequestModel> lateRequestOptional = lateRequestRepository.findById(lateRequestId);

        if (lateRequestOptional.isPresent()) {
            LateRequestModel lateRequest = lateRequestOptional.get();

            if (lateRequest.getStatus() == LateRequestStatus.PENDING) {
                lateRequest.setStatus(LateRequestStatus.APPROVED);
                lateRequestRepository.save(lateRequest);
                return new ResponseEntity<>("Late request approved", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Late request is already processed", HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity<>("Late request not found", HttpStatus.NOT_FOUND);
        }
    }


    public ResponseEntity<?> rejectLateRequest(Long lateRequestId) {
        Optional<LateRequestModel> lateRequestOptional = lateRequestRepository.findById(lateRequestId);

        if (lateRequestOptional.isPresent()) {
            LateRequestModel lateRequest = lateRequestOptional.get();

            if (lateRequest.getStatus() == LateRequestStatus.PENDING) {
                lateRequest.setStatus(LateRequestStatus.REJECTED);
                lateRequestRepository.save(lateRequest);
                return new ResponseEntity<>("Late request rejected", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Late request is already processed", HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity<>("Late request not found", HttpStatus.NOT_FOUND);
        }
    }




    public Optional<AdminModel> findByEmailAndPassword(String email, String password) {
        return adminRepository.findByEmailAndPassword(email, password);
    }

    public void updateAdminToken(AdminModel adminModel) {
        adminRepository.save(adminModel); // Save the updated admin model with the token
    }

    public ResponseEntity<?> updateAdmin(int id, AdminDto adminDto) {
        Optional<AdminModel> existingAdmin = adminRepository.findById(id);
        if (existingAdmin.isPresent()) {
            AdminModel adminToUpdate = existingAdmin.get();
            adminToUpdate.setName(adminDto.getName());
            adminToUpdate.setEmail(adminDto.getEmail());
            adminToUpdate.setPassword(adminDto.getPassword());

            adminRepository.save(adminToUpdate);
            return new ResponseEntity<>(adminToUpdate, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Admin not found", HttpStatus.NOT_FOUND);
        }
    }




    public boolean deleteAdmin(Long id) {
        Optional<AdminModel> existingAdmin = adminRepository.findById(id);
        if (!existingAdmin.isPresent()) {
            return false;
        }
        adminRepository.delete(existingAdmin.get());
        return true;
    }


    public AdminModel updateAdminPassword(Long id, AdminDto adminDto) throws Exception {
        Optional<AdminModel> existingAdmin = adminRepository.findById(id);
        if (!existingAdmin.isPresent()) {
            throw new Exception("Admin Not Found");
        }
        AdminModel admin = existingAdmin.get();
        admin.setPassword(adminDto.getPassword());
        return adminRepository.save(admin);
    }

    public ResponseEntity<?> forgotPassword(ForgotPasswordDto forgotPasswordDto) {
        Optional<AdminModel> adminOptional = adminRepository.findByEmail(forgotPasswordDto.getEmail());
        if (adminOptional.isPresent()) {
            String temporaryPassword = UUID.randomUUID().toString().substring(0, 8);
            AdminModel admin = adminOptional.get();
            admin.setPassword(temporaryPassword);
            adminRepository.save(admin);
            sendForgotPasswordEmail(admin.getEmail(), temporaryPassword);

            return new ResponseEntity<>("Temporary password sent to your email", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Email not found", HttpStatus.NOT_FOUND);
        }
    }


    private void sendForgotPasswordEmail(String toEmail, String temporaryPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Forgot Password Assistance");
        message.setText("Your temporary password is: " + temporaryPassword + ". Please use it to log in and reset your password.");

        mailSender.send(message);
    }

    public ResponseEntity<?> deleteAttendanceForMonth(int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        List<Attendance> attendanceRecords = attendanceRepository.findByAttendanceDateBetween(startDate, endDate);

        if (attendanceRecords.isEmpty()) {
            return new ResponseEntity<>("No attendance records found for the specified month", HttpStatus.NOT_FOUND);
        }

        attendanceRepository.deleteByAttendanceDateBetween(startDate, endDate);

        return new ResponseEntity<>("Attendance records for the month deleted successfully", HttpStatus.OK);
    }



    public ResponseEntity<?> deleteLeaveRequestsForMonth(int year, int month) {
        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());
        List<LeaveRequestModel> leaveRequests = leaveRequestRepository.findByFromDateBetween(startOfMonth, endOfMonth);
        if (leaveRequests.isEmpty()) {
            return new ResponseEntity<>("No leave records found for the specified month", HttpStatus.NOT_FOUND);
        }
        leaveRequestRepository.deleteByFromDateBetween(startOfMonth, endOfMonth);
        return new ResponseEntity<>("Leave requests for the specified month have been deleted successfully", HttpStatus.OK);
    }



}
