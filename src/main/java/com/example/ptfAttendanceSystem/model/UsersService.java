package com.example.ptfAttendanceSystem.model;





import com.example.ptfAttendanceSystem.InAndOut.InAndOutRepository;
import com.example.ptfAttendanceSystem.InAndOut.InOut;
import com.example.ptfAttendanceSystem.attendance.Attendance;
import com.example.ptfAttendanceSystem.attendance.AttendanceRepository;
import com.example.ptfAttendanceSystem.batch.BatchModel;
import com.example.ptfAttendanceSystem.batch.BatchRepository;
import com.example.ptfAttendanceSystem.batchType.BatchTypeModel;
import com.example.ptfAttendanceSystem.batchType.BatchTypeRepository;
import com.example.ptfAttendanceSystem.late.LateRequestModel;
import com.example.ptfAttendanceSystem.late.LateRequestRepository;
import com.example.ptfAttendanceSystem.late.LateRequestStatus;
import com.example.ptfAttendanceSystem.late_table.LateAttendance;
import com.example.ptfAttendanceSystem.late_table.LateAttendanceRepository;
import com.example.ptfAttendanceSystem.leave.*;
import com.example.ptfAttendanceSystem.qr.QRCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service

public class UsersService {


    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private LateRequestRepository lateRequestRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private QRCodeService qrCodeService;

    @Autowired
    private InAndOutRepository inAndOutRepository;

    @Autowired
    private BatchTypeRepository batchTypeRepository;

    @Autowired
    private LateAttendanceRepository lateAttendanceRepository;

    private final UsersRepository usersRepository;
    private final BatchRepository batchRepository;

    @Autowired
    public UsersService(UsersRepository usersRepository, BatchRepository batchRepository) {
        this.usersRepository = usersRepository;
        this.batchRepository = batchRepository;
    }

    public ResponseEntity<?> userRegistration(UserDto userDto) {
        // Validate required fields
        if (userDto.getName() == null || userDto.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Please enter your name.");
        }
        if (userDto.getEmail() == null || userDto.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Please enter your email.");
        }
        if (userDto.getPassword() == null || userDto.getPassword().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Please enter your password.");
        }
        if (userDto.getBatchId() == null) {
            return ResponseEntity.badRequest().body("Please select a batch.");
        }
        if (userDto.getPhoneNumber() == null || userDto.getPhoneNumber().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Please enter your phone number.");
        }
        if (usersRepository.findByEmail(userDto.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User already registered");
        }

        UsersModel user = new UsersModel();
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        user.setPassword(userDto.getPassword());
        user.setBatchId(userDto.getBatchId());
        user.setPhoneNumber(userDto.getPhoneNumber());

        UsersModel savedUser = usersRepository.save(user);

        if (!savedUser.getEmail().isEmpty()) {
            sendRegistrationEmail(savedUser.getEmail());
        } else {
            return ResponseEntity.badRequest().body("Email is required");
        }


        BatchModel batch = batchRepository.findById(savedUser.getBatchId()).orElse(null);


        URegistrationResponse response = UserMapper.toResponse(savedUser, batch);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private void sendRegistrationEmail(String toEmail) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Registration Confirmation");
        message.setText("Thank you for registering for the PTF application!");
        mailSender.send(message);
    }


    public ResponseEntity<?> loginUser(LoginDto loginDto) {
        Optional<UsersModel> userOpt = usersRepository.findByEmailAndPassword(loginDto.getEmail(), loginDto.getPassword());

        if (userOpt.isPresent()) {
            UsersModel userModel = userOpt.get();
            userModel.setToken(UUID.randomUUID().toString());
            usersRepository.save(userModel);


            BatchModel batch = batchRepository.findById(userModel.getBatchId()).orElse(null);

            LoginResponseDto responseDto = new LoginResponseDto(
                    userModel.getUserId(),
                    userModel.getEmail(),
                    userModel.getName(),
                    userModel.getBatchId(),
                    batch != null ? batch.getBatchName() : null,
                    userModel.getToken(),
                    "Login Successfully"
            );

            return ResponseEntity.accepted().body(responseDto);
        } else {
            return ResponseEntity.badRequest().body("No details found");
        }
    }



//    public ResponseEntity<?> scanInAndOut(Long userId, InScanDto inScanDto) {
//        Map<String, Object> response = new HashMap<>();
//        Optional<UsersModel> usersModelOptional = usersRepository.findById(userId);
//        if (!usersModelOptional.isPresent()) {
////            response.put("success", false);
//            response.put("message", "Invalid UserId");
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
////            return new ResponseEntity<>("Invalid UserId", HttpStatus.NOT_FOUND);
//        }
//
//        UsersModel usersModel = usersModelOptional.get();
//        Optional<BatchModel> batchOptional = batchRepository.findById(usersModel.getBatchId());
//
//        if (!batchOptional.isPresent()) {
////            response.put("success", false);
//            response.put("message", "Invalid Batch ID");
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
////            return new ResponseEntity<>("Invalid Batch ID", HttpStatus.NOT_FOUND);
//        }
//
//        BatchModel batch = batchOptional.get();
//        Optional<BatchTypeModel> batchTypeOptional = batchTypeRepository.findById(batch.getBatchType().getId());
//
//        if (!batchTypeOptional.isPresent()) {
////            response.put("success", false);
//            response.put("message", "Batch type not found");
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
////            return new ResponseEntity<>("Batch type not found", HttpStatus.NOT_FOUND);
//        }
//
//
//        BatchTypeModel batchType = batchTypeOptional.get();
//
//
//
//
//        String typeData = inScanDto.getType();
//
//        try {
//            if ("in".equalsIgnoreCase(typeData)) {
//                if ("Custom".equalsIgnoreCase(batchType.getBatchType())) {
//                    return handleCustomBatchScanIn(userId, usersModel.getName(), batch, batchType, inScanDto);
//                } else if ("Regular".equalsIgnoreCase(batchType.getBatchType())) {
//                    return handleRegularBatchScanIn(userId, usersModel.getName(), batch, inScanDto);
//                } else {
////                    response.put("success", false);
//                    response.put("message", "Invalid Batch Type");
//                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
////                    return new ResponseEntity<>("Invalid Batch Type", HttpStatus.BAD_REQUEST);
//                }
//            } else if ("out".equalsIgnoreCase(typeData)) {
//                return handleScanOut(userId, inScanDto, batchType);
//            } else {
////                response.put("success", false);
//                response.put("message", "Scan Type is not mentioned");
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
////                return new ResponseEntity<>("Scan Type is not mentioned", HttpStatus.BAD_REQUEST);
//            }
//        } finally {
//            qrCodeService.regenerateQRCode();
//        }
//    }
//
//
//    private static final double EARTH_RADIUS_METERS = 6371000;
//
//    private boolean isWithinAllowedDistance(String lat1Str, String lon1Str, String lat2Str, String lon2Str, double maxDistanceMeters) {
//        try {
//            double lat1 = Double.parseDouble(lat1Str);
//            double lon1 = Double.parseDouble(lon1Str);
//            double lat2 = Double.parseDouble(lat2Str);
//            double lon2 = Double.parseDouble(lon2Str);
//
//            double latDistance = Math.toRadians(lat2 - lat1);
//            double lonDistance = Math.toRadians(lon2 - lon1);
//
//            double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
//                    Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
//                            Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
//
//            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
//            double distance = EARTH_RADIUS_METERS * c;
//
//            return distance <= maxDistanceMeters;
//        } catch (NumberFormatException e) {
//            return false;
//        }
//    }
//
//
//
//    private ResponseEntity<?> handleCustomBatchScanIn(Long userId, String userName, BatchModel batch, BatchTypeModel batchType, InScanDto inScanDto) {
//        Map<String, Object> response = new HashMap<>();
//        LocalDate currentDate = LocalDate.now();
//        if (!currentDate.equals(inScanDto.getPresentDate())) {
////            response.put("success", false);
//            response.put("message", "Current date is not correct");
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
////            return new ResponseEntity<>("Current date is not correct", HttpStatus.BAD_REQUEST);
//        }
//
//        Optional<Attendance> existingAttendance = attendanceRepository.findByUserIdAndAttendanceDate(userId, currentDate);
//        if (existingAttendance.isPresent() && existingAttendance.get().getScanInTime() != null) {
////            response.put("success", false);
//            response.put("message", "User has already scanned in today");
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
////            return new ResponseEntity<>("User has already scanned in today", HttpStatus.BAD_REQUEST);
//        }
//
//
//
//        if (!isWithinAllowedDistance(batch.getBatchLatitude(), batch.getBatchLongitude(),
//                inScanDto.getUserLatitude(), inScanDto.getUserLongitude(), 5)) {
////            response.put("success", false);
//            response.put("message", "Scan in rejected: You are not within the allowed range.");
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
////            return new ResponseEntity<>("Scan in rejected: You are not within the allowed range.", HttpStatus.BAD_REQUEST);
//        }
//
//
//        Optional<LateRequestModel> lateRequestOptional = lateRequestRepository.findByUserIdAndDate(userId, currentDate);
//        if (lateRequestOptional.isPresent() && lateRequestOptional.get().getStatus() == LateRequestStatus.APPROVED) {
//            return markApprovedLateAttendance(userId, userName, batch, currentDate, batchType);
//        }
//
//        List<LateAttendance> lateRecords = lateAttendanceRepository.findByUserId(userId);
//        if (lateRecords.size() >= 4) {
//            lateAttendanceRepository.deleteAll(lateRecords);
////            response.put("success", false);
//            response.put("message", "Attendance not marked. You have been marked as leave due to 4 late attendances.");
//            return ResponseEntity.status(HttpStatus.OK).body(response);
////            return new ResponseEntity<>("Attendance not marked. You have been marked as leave due to 4 late attendances.", HttpStatus.BAD_REQUEST);
//        }
//
//        LocalTime allowedTime = batch.getStartTime().plusMinutes(10);
//        boolean isLate = inScanDto.getPresentTime().isAfter(allowedTime);
//
//        Attendance attendance = new Attendance();
//        attendance.setUserId(userId);
//        attendance.setUserName(userName);
//        attendance.setBatchName(batch.getBatchName());
//        attendance.setAttendanceDate(currentDate);
//        attendance.setScanInTime(inScanDto.getPresentTime());
//        attendance.setStatus(isLate ? "Late" : "Punctual");
//        attendanceRepository.save(attendance);
//
//        if (isLate) {
//            saveLateUser(userId, userName, batch.getBatchName(), inScanDto, "Late");
//        }
//
////        response.put("success", true);
//        response.put("message", isLate ? "Late Attendance Marked" : "Scan In Successful");
//        return ResponseEntity.ok(response);
//
////        return new ResponseEntity<>(isLate ? "Late Attendance Marked" : "Scan In Successful", HttpStatus.OK);
//    }
//
//
//
//    private ResponseEntity<?> handleRegularBatchScanIn(Long userId, String userName, BatchModel batch, InScanDto inScanDto) {
//        Map<String, Object> response = new HashMap<>();
//        LocalDate currentDate = LocalDate.now();
//        if (!currentDate.equals(inScanDto.getPresentDate())) {
////            response.put("success", false);
//            response.put("message", "Current date is not correct");
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
////            return new ResponseEntity<>("Current date is not correct", HttpStatus.BAD_REQUEST);
//        }
//
//        Optional<Attendance> existingAttendance = attendanceRepository.findByUserIdAndAttendanceDate(userId, currentDate);
//        if (existingAttendance.isPresent() && existingAttendance.get().getScanInTime() != null) {
////            response.put("success", false);
//            response.put("message", "User has already scanned in today");
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
////            return new ResponseEntity<>("User has already scanned in today", HttpStatus.BAD_REQUEST);
//        }
//
////
//
//        if (!isWithinAllowedDistance(batch.getBatchLatitude(), batch.getBatchLongitude(),
//                inScanDto.getUserLatitude(), inScanDto.getUserLongitude(), 5)) {
////            response.put("success", false);
//            response.put("message", "Scan in rejected: You are not within the allowed range.");
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
////            return new ResponseEntity<>("Scan in rejected: You are not within the allowed range.", HttpStatus.BAD_REQUEST);
//        }
//
//        LocalTime allowedTime = batch.getStartTime().plusMinutes(01);
//        boolean isLate = inScanDto.getPresentTime().isAfter(allowedTime);
//
//        Attendance attendance = new Attendance();
//        attendance.setUserId(userId);
//        attendance.setUserName(userName);
//        attendance.setBatchName(batch.getBatchName());
//        attendance.setAttendanceDate(currentDate);
//        attendance.setScanInTime(inScanDto.getPresentTime());
//        attendance.setStatus(isLate ? "Late" : "Punctual");
//        attendanceRepository.save(attendance);
//
//        if (isLate) {
//            saveLateUser(userId, userName, batch.getBatchName(), inScanDto, "Late");
//        }
////        response.put("success", true);
//        response.put("message", isLate ? "Late Attendance Marked" : "Scan In Successful");
//        return ResponseEntity.ok(response);
//
////        return new ResponseEntity<>(isLate ? "Late Attendance Marked" : "Scan In Successful", HttpStatus.OK);
//    }
//
//
//
//
//
//
//    private ResponseEntity<?> markApprovedLateAttendance(Long userId, String userName, BatchModel batch, LocalDate currentDate, BatchTypeModel batchType) {
//        Map<String, Object> response = new HashMap<>();
//        if ("Regular".equalsIgnoreCase(batchType.getBatchType())) {
//
//            LocalTime allowedTime = batch.getStartTime().plusMinutes(30);
//            Attendance attendance = new Attendance();
//            attendance.setUserId(userId);
//            attendance.setUserName(userName);
//            attendance.setBatchName(batch.getBatchName());
//            attendance.setAttendanceDate(currentDate);
//            attendance.setScanInTime(allowedTime);
//            attendance.setStatus("Punctual");
//
//            attendanceRepository.save(attendance);
//
//
////            response.put("success", true);
//            response.put("message", "Scan In Successful (Late Request Approved for " + batchType.getBatchType() + " Batch)");
//
//            return ResponseEntity.ok(response);
////            return new ResponseEntity<>("Scan In Successful (Late Request Approved for Regular Batch)", HttpStatus.OK);
//        }
//
//        LocalTime allowedTime = batch.getStartTime().plusMinutes(10);
//        Attendance attendance = new Attendance();
//        attendance.setUserId(userId);
//        attendance.setUserName(userName);
//        attendance.setBatchName(batch.getBatchName());
//        attendance.setAttendanceDate(currentDate);
//        attendance.setScanInTime(allowedTime);
//        attendance.setStatus("Punctual");
//
//        attendanceRepository.save(attendance);
//
//        //            response.put("success", true);
//        response.put("message", "Scan In Successful (Late Request Approved for " + batchType.getBatchType() + " Batch)");
//
//        return ResponseEntity.ok(response);
////
////        return new ResponseEntity<>("Scan In Successful (Late Request Approved for Custom Batch)", HttpStatus.OK);
//    }
//
//    private void saveLateUser(Long userId, String userName, String batchType, InScanDto inScanDto, String status) {
//        LateAttendance lateAttendance = new LateAttendance();
//        lateAttendance.setUserId(userId);
//        lateAttendance.setUserName(userName);
//        lateAttendance.setBatchType(batchType);
//        lateAttendance.setAttendanceDate(inScanDto.getPresentDate());
//        lateAttendance.setScanInTime(inScanDto.getPresentTime());
//        lateAttendance.setReasonForLateness("Arrived after allowed time");
//        lateAttendance.setStatus(status);
//
//        lateAttendanceRepository.save(lateAttendance);
//    }
//    private ResponseEntity<?> handleScanOut(Long userId, InScanDto inScanDto, BatchTypeModel batchType) {
//        Map<String, Object> response = new HashMap<>();
//        LocalDate currentDate = LocalDate.now();
//
//        if (!currentDate.equals(inScanDto.getPresentDate())) {
//
//
////            response.put("success", false);
//            response.put("message", "Scan Out Date is not correct");
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
//
////            return new ResponseEntity<>("Scan Out Date is not correct", HttpStatus.BAD_REQUEST);
//        }
//
//        Optional<UsersModel> usersModelOptional = usersRepository.findById(userId);
//        if (!usersModelOptional.isPresent()) {
////            response.put("success", false);
//            response.put("message", "Invalid UserId");
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
////            return new ResponseEntity<>("Invalid UserId", HttpStatus.NOT_FOUND);
//        }
//
//        UsersModel usersModel = usersModelOptional.get();
//        Optional<BatchModel> batchOptional = batchRepository.findById(usersModel.getBatchId());
//
//        if (!batchOptional.isPresent()) {
////            response.put("success", false);
//            response.put("message", "Invalid Batch ID");
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
////            return new ResponseEntity<>("Invalid Batch ID", HttpStatus.NOT_FOUND);
//        }
//
//        BatchModel batch = batchOptional.get();
//
//
//
//        Optional<Attendance> attendanceOptional = attendanceRepository.findByUserIdAndAttendanceDate(userId, currentDate);
//
//        if (!attendanceOptional.isPresent() || attendanceOptional.get().getScanInTime() == null) {
//
////            response.put("success", false);
//            response.put("message", "User must scan in before scanning out");
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
////            return new ResponseEntity<>("User must scan in before scanning out", HttpStatus.BAD_REQUEST);
//        }
//
//        if (!isWithinAllowedDistance(batch.getBatchLatitude(), batch.getBatchLongitude(),
//                inScanDto.getUserLatitude(), inScanDto.getUserLongitude(), 5)) {
//
////            response.put("success", false);
//            response.put("message", "Scan out rejected: You are not within the allowed range.");
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
////            return new ResponseEntity<>("Scan out rejected: You are not within the allowed range.", HttpStatus.BAD_REQUEST);
//
//        }
//
//        Attendance attendance = attendanceOptional.get();
//
//        if (attendance.getScanOutTime() != null) {
//            response.put("success", false);
//            response.put("message", "User has already scanned out today");
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
////            return new ResponseEntity<>("User has already scanned out today", HttpStatus.BAD_REQUEST);
//        }
//
//        attendance.setScanOutTime(inScanDto.getPresentTime());
//        attendanceRepository.save(attendance);
//        response.put("success", true);
//        response.put("message", "Scan Out Successful");
//        return ResponseEntity.ok(response);
////        return new ResponseEntity<>("Scan Out Successful", HttpStatus.OK);
//    }

    public ResponseEntity<?> scanInAndOut(Long userId, InScanDto inScanDto) {
        Map<String, Object> response = new HashMap<>();
        Optional<UsersModel> usersModelOptional = usersRepository.findById(userId);
        if (!usersModelOptional.isPresent()) {
//            response.put("success", false);
            response.put("message", "Invalid UserId");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
//            return new ResponseEntity<>("Invalid UserId", HttpStatus.NOT_FOUND);
        }

        UsersModel usersModel = usersModelOptional.get();
        Optional<BatchModel> batchOptional = batchRepository.findById(usersModel.getBatchId());

        if (!batchOptional.isPresent()) {
//            response.put("success", false);
            response.put("message", "Invalid Batch ID");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
//            return new ResponseEntity<>("Invalid Batch ID", HttpStatus.NOT_FOUND);
        }

        BatchModel batch = batchOptional.get();
        Optional<BatchTypeModel> batchTypeOptional = batchTypeRepository.findById(batch.getBatchType().getId());

        if (!batchTypeOptional.isPresent()) {
//            response.put("success", false);
            response.put("message", "Batch type not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
//            return new ResponseEntity<>("Batch type not found", HttpStatus.NOT_FOUND);
        }


        BatchTypeModel batchType = batchTypeOptional.get();


        String typeData = inScanDto.getType();

        try {
            if ("in".equalsIgnoreCase(typeData)) {
                if ("Custom".equalsIgnoreCase(batchType.getBatchType())) {
                    return handleCustomBatchScanIn(userId, usersModel.getName(), batch, batchType, inScanDto);
                } else if ("Regular".equalsIgnoreCase(batchType.getBatchType())) {
                    return handleRegularBatchScanIn(userId, usersModel.getName(), batch, inScanDto);
                } else {
//                    response.put("success", false);
                    response.put("message", "Invalid Batch Type");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
//                    return new ResponseEntity<>("Invalid Batch Type", HttpStatus.BAD_REQUEST);
                }
            } else if ("out".equalsIgnoreCase(typeData)) {
                return handleScanOut(userId, inScanDto, batchType);
            } else {
//                response.put("success", false);
                response.put("message", "Scan Type is not mentioned");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
//                return new ResponseEntity<>("Scan Type is not mentioned", HttpStatus.BAD_REQUEST);
            }
        } finally {
            qrCodeService.regenerateQRCode();
        }
    }


    private static final double EARTH_RADIUS_METERS = 6371000;

    private boolean isWithinAllowedDistance(String lat1Str, String lon1Str, String lat2Str, String lon2Str, double maxDistanceMeters) {
        try {
            double lat1 = Double.parseDouble(lat1Str);
            double lon1 = Double.parseDouble(lon1Str);
            double lat2 = Double.parseDouble(lat2Str);
            double lon2 = Double.parseDouble(lon2Str);

            double latDistance = Math.toRadians(lat2 - lat1);
            double lonDistance = Math.toRadians(lon2 - lon1);

            double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                    Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                            Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            double distance = EARTH_RADIUS_METERS * c;

            return distance <= maxDistanceMeters;
        } catch (NumberFormatException e) {
            return false;
        }
    }


    private ResponseEntity<?> handleCustomBatchScanIn(Long userId, String userName, BatchModel batch, BatchTypeModel batchType, InScanDto inScanDto) {
        Map<String, Object> response = new HashMap<>();
        LocalDate currentDate = LocalDate.now();
        if (!currentDate.equals(inScanDto.getPresentDate())) {
//            response.put("success", false);
            response.put("message", "Current date is not correct");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
//            return new ResponseEntity<>("Current date is not correct", HttpStatus.BAD_REQUEST);
        }

        Optional<Attendance> existingAttendance = attendanceRepository.findByUserIdAndAttendanceDate(userId, currentDate);
        if (existingAttendance.isPresent() && existingAttendance.get().getScanInTime() != null) {
//            response.put("success", false);
            response.put("message", "User has already scanned in today");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
//            return new ResponseEntity<>("User has already scanned in today", HttpStatus.BAD_REQUEST);
        }


        if (!isWithinAllowedDistance(batch.getBatchLatitude(), batch.getBatchLongitude(),
                inScanDto.getUserLatitude(), inScanDto.getUserLongitude(), 5)) {
//            response.put("success", false);
            response.put("message", "Scan in rejected: You are not within the allowed range.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
//            return new ResponseEntity<>("Scan in rejected: You are not within the allowed range.", HttpStatus.BAD_REQUEST);
        }


        Optional<LateRequestModel> lateRequestOptional = lateRequestRepository.findByUserIdAndDate(userId, currentDate);
        if (lateRequestOptional.isPresent() && lateRequestOptional.get().getStatus() == LateRequestStatus.APPROVED) {
            return markApprovedLateAttendance(userId, userName, batch, currentDate, batchType);
        }

        List<LateAttendance> lateRecords = lateAttendanceRepository.findByUserId(userId);
        if (lateRecords.size() >= 4) {
            lateAttendanceRepository.deleteAll(lateRecords);
//            response.put("success", false);
            response.put("message", "Attendance not marked. You have been marked as leave due to 4 late attendances.");
            return ResponseEntity.status(HttpStatus.OK).body(response);
//            return new ResponseEntity<>("Attendance not marked. You have been marked as leave due to 4 late attendances.", HttpStatus.BAD_REQUEST);
        }

        LocalTime allowedTime = batch.getStartTime().plusMinutes(10);
        boolean isLate = inScanDto.getPresentTime().isAfter(allowedTime);

        Attendance attendance = new Attendance();
        attendance.setUserId(userId);
        attendance.setUserName(userName);
        attendance.setBatchName(batch.getBatchName());
        attendance.setAttendanceDate(currentDate);
        attendance.setScanInTime(inScanDto.getPresentTime());
        attendance.setStatus(isLate ? "Late" : "Punctual");
        attendanceRepository.save(attendance);

        if (isLate) {
            saveLateUser(userId, userName, batch.getBatchName(), inScanDto, "Late");
        }

//        response.put("success", true);
        response.put("message", isLate ? "Late Attendance Marked" : "Scan In Successful");
        return ResponseEntity.ok(response);

//        return new ResponseEntity<>(isLate ? "Late Attendance Marked" : "Scan In Successful", HttpStatus.OK);
    }


    private ResponseEntity<?> handleRegularBatchScanIn(Long userId, String userName, BatchModel batch, InScanDto inScanDto) {
        Map<String, Object> response = new HashMap<>();
        LocalDate currentDate = LocalDate.now();
        if (!currentDate.equals(inScanDto.getPresentDate())) {
//            response.put("success", false);
            response.put("message", "Current date is not correct");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
//            return new ResponseEntity<>("Current date is not correct", HttpStatus.BAD_REQUEST);
        }

        Optional<Attendance> existingAttendance = attendanceRepository.findByUserIdAndAttendanceDate(userId, currentDate);
        if (existingAttendance.isPresent() && existingAttendance.get().getScanInTime() != null) {
//            response.put("success", false);
            response.put("message", "User has already scanned in today");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
//            return new ResponseEntity<>("User has already scanned in today", HttpStatus.BAD_REQUEST);
        }

//

        if (!isWithinAllowedDistance(batch.getBatchLatitude(), batch.getBatchLongitude(),
                inScanDto.getUserLatitude(), inScanDto.getUserLongitude(), 5)) {
//            response.put("success", false);
            response.put("message", "Scan in rejected: You are not within the allowed range.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
//            return new ResponseEntity<>("Scan in rejected: You are not within the allowed range.", HttpStatus.BAD_REQUEST);
        }

        LocalTime allowedTime = batch.getStartTime().plusMinutes(01);
        boolean isLate = inScanDto.getPresentTime().isAfter(allowedTime);

        Attendance attendance = new Attendance();
        attendance.setUserId(userId);
        attendance.setUserName(userName);
        attendance.setBatchName(batch.getBatchName());
        attendance.setAttendanceDate(currentDate);
        attendance.setScanInTime(inScanDto.getPresentTime());
        attendance.setStatus(isLate ? "Late" : "Punctual");
        attendanceRepository.save(attendance);

        if (isLate) {
            saveLateUser(userId, userName, batch.getBatchName(), inScanDto, "Late");
        }
//        response.put("success", true);
        response.put("message", isLate ? "Late Attendance Marked" : "Scan In Successful");
        return ResponseEntity.ok(response);

//        return new ResponseEntity<>(isLate ? "Late Attendance Marked" : "Scan In Successful", HttpStatus.OK);
    }


    private ResponseEntity<?> markApprovedLateAttendance(Long userId, String userName, BatchModel batch, LocalDate currentDate, BatchTypeModel batchType) {
        Map<String, Object> response = new HashMap<>();
        if ("Regular".equalsIgnoreCase(batchType.getBatchType())) {

            LocalTime allowedTime = batch.getStartTime().plusMinutes(30);
            Attendance attendance = new Attendance();
            attendance.setUserId(userId);
            attendance.setUserName(userName);
            attendance.setBatchName(batch.getBatchName());
            attendance.setAttendanceDate(currentDate);
            attendance.setScanInTime(allowedTime);
            attendance.setStatus("Punctual");

            attendanceRepository.save(attendance);


//            response.put("success", true);
            response.put("message", "Scan In Successful (Late Request Approved for " + batchType.getBatchType() + " Batch)");

            return ResponseEntity.ok(response);
//            return new ResponseEntity<>("Scan In Successful (Late Request Approved for Regular Batch)", HttpStatus.OK);
        }

        LocalTime allowedTime = batch.getStartTime().plusMinutes(10);
        Attendance attendance = new Attendance();
        attendance.setUserId(userId);
        attendance.setUserName(userName);
        attendance.setBatchName(batch.getBatchName());
        attendance.setAttendanceDate(currentDate);
        attendance.setScanInTime(allowedTime);
        attendance.setStatus("Punctual");

        attendanceRepository.save(attendance);

        //            response.put("success", true);
        response.put("message", "Scan In Successful (Late Request Approved for " + batchType.getBatchType() + " Batch)");

        return ResponseEntity.ok(response);
//
//        return new ResponseEntity<>("Scan In Successful (Late Request Approved for Custom Batch)", HttpStatus.OK);
    }

    private void saveLateUser(Long userId, String userName, String batchType, InScanDto inScanDto, String status) {
        LateAttendance lateAttendance = new LateAttendance();
        lateAttendance.setUserId(userId);
        lateAttendance.setUserName(userName);
        lateAttendance.setBatchType(batchType);
        lateAttendance.setAttendanceDate(inScanDto.getPresentDate());
        lateAttendance.setScanInTime(inScanDto.getPresentTime());
        lateAttendance.setReasonForLateness("Arrived after allowed time");
        lateAttendance.setStatus(status);

        lateAttendanceRepository.save(lateAttendance);
    }

    private ResponseEntity<?> handleScanOut(Long userId, InScanDto inScanDto, BatchTypeModel batchType) {
        Map<String, Object> response = new HashMap<>();
        LocalDate currentDate = LocalDate.now();

        if (!currentDate.equals(inScanDto.getPresentDate())) {


//            response.put("success", false);
            response.put("message", "Scan Out Date is not correct");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

//            return new ResponseEntity<>("Scan Out Date is not correct", HttpStatus.BAD_REQUEST);
        }

        Optional<UsersModel> usersModelOptional = usersRepository.findById(userId);
        if (!usersModelOptional.isPresent()) {
//            response.put("success", false);
            response.put("message", "Invalid UserId");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
//            return new ResponseEntity<>("Invalid UserId", HttpStatus.NOT_FOUND);
        }

        UsersModel usersModel = usersModelOptional.get();
        Optional<BatchModel> batchOptional = batchRepository.findById(usersModel.getBatchId());

        if (!batchOptional.isPresent()) {
//            response.put("success", false);
            response.put("message", "Invalid Batch ID");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
//            return new ResponseEntity<>("Invalid Batch ID", HttpStatus.NOT_FOUND);
        }

        BatchModel batch = batchOptional.get();


        Optional<Attendance> attendanceOptional = attendanceRepository.findByUserIdAndAttendanceDate(userId, currentDate);

        if (!attendanceOptional.isPresent() || attendanceOptional.get().getScanInTime() == null) {

//            response.put("success", false);
            response.put("message", "User must scan in before scanning out");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
//            return new ResponseEntity<>("User must scan in before scanning out", HttpStatus.BAD_REQUEST);
        }

        if (!isWithinAllowedDistance(batch.getBatchLatitude(), batch.getBatchLongitude(),
                inScanDto.getUserLatitude(), inScanDto.getUserLongitude(), 5)) {

//            response.put("success", false);
            response.put("message", "Scan out rejected: You are not within the allowed range.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
//            return new ResponseEntity<>("Scan out rejected: You are not within the allowed range.", HttpStatus.BAD_REQUEST);

        }

        Attendance attendance = attendanceOptional.get();

        if (attendance.getScanOutTime() != null) {
            response.put("success", false);
            response.put("message", "User has already scanned out today");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
//            return new ResponseEntity<>("User has already scanned out today", HttpStatus.BAD_REQUEST);
        }

        attendance.setScanOutTime(inScanDto.getPresentTime());
        attendanceRepository.save(attendance);
        response.put("success", true);
        response.put("message", "Scan Out Successful");
        return ResponseEntity.ok(response);
//        return new ResponseEntity<>("Scan Out Successful", HttpStatus.OK);
    }

    public List<Attendance> getAllAttendanceByDate(LocalDate date) {
        return attendanceRepository.findByAttendanceDate(date);
    }



    public List<Attendance> getAttendanceForMonth(Long userId, int month, int year) {
        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());
        return attendanceRepository.findByUserIdAndAttendanceDateBetween(userId, startOfMonth, endOfMonth);
    }

    public Optional<UsersModel> findUserByToken(String token) {
        return usersRepository.findByToken(token);
    }


    public List<LeaveRequestModel> getLeaveRequestsByUserId(Long userId) {
        return leaveRequestRepository.findByUserId(userId);
    }


    public Optional<UsersModel> findByEmailAndPassword(String email, String password) {
        return usersRepository.findByEmailAndPassword(email, password);
    }


    public void updateUserToken(UsersModel usersModel) {
        usersRepository.save(usersModel);
    }


    public boolean isBatchExists(Long batchId) {
        return batchRepository.existsById(batchId);
    }

    public List<GetAllUsersDTO> getAllUsers(Long batchId) {
        List<UsersModel> users;

        if (batchId != null) {
            users = usersRepository.findByBatchId(batchId);
        } else {
            users = usersRepository.findAll();
        }

        return users.stream().map(user -> {
            String batchName = batchRepository.findById(user.getBatchId())
                    .map(BatchModel::getBatchName)
                    .orElse("Unknown");
            return new GetAllUsersDTO(
                    user.getUserId(),
                    user.getName(),
                    user.getEmail(),
                    user.getPhoneNumber(),
                    user.getBatchId(),
                    batchName
            );
        }).collect(Collectors.toList());
    }




    public Optional<UsersModel> getUserById(Long id) {
        return usersRepository.findById(id);
    }

    public List<LateRequestModel> getLateRequestsByUserId(Long userId) {
        return lateRequestRepository.findByUserId(userId);
    }

    public Optional<UsersModel> findByEmail(String email) {
        return usersRepository.findByEmail(email);
    }


    public UsersModel updateUserPassword(Long id, UserDto userDto) throws Exception {
        Optional<UsersModel> existingUser = usersRepository.findById(id);
        if (!existingUser.isPresent()) {
            throw new Exception("User Not Found");
        }
        UsersModel user = existingUser.get();
        user.setPassword(userDto.getPassword());
        return usersRepository.save(user);
    }


    public boolean deleteUser(Long id) {
        Optional<UsersModel> user = usersRepository.findById(id);
        if (user.isPresent()) {
            usersRepository.deleteById(id);
            return true;
        }
        return false;
    }



    public ResponseEntity<?> forgotPassword(ForgotPasswordDto forgotPasswordDto) {
        Optional<UsersModel> userOptional = usersRepository.findByEmail(forgotPasswordDto.getEmail());
        if (userOptional.isPresent()) {
            String temporaryPassword = UUID.randomUUID().toString().substring(0, 8);
            UsersModel user = userOptional.get();
            user.setPassword(temporaryPassword);
            usersRepository.save(user);

            sendForgotPasswordEmail(user.getEmail(), temporaryPassword);

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



    public ResponseEntity<?> updateUser(Long userId, UpdateUserDto updateUserDto) {

        Optional<UsersModel> optionalUser = usersRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        UsersModel user = optionalUser.get();

        if (updateUserDto.getName() != null && !updateUserDto.getName().isEmpty()) {
            user.setName(updateUserDto.getName());
        }
        if (updateUserDto.getEmail() != null && !updateUserDto.getEmail().isEmpty()) {
            user.setEmail(updateUserDto.getEmail());
        }
        if (updateUserDto.getPassword() != null && !updateUserDto.getPassword().isEmpty()) {
            user.setPassword(updateUserDto.getPassword());
        }
        if (updateUserDto.getPhoneNumber() != null && !updateUserDto.getPhoneNumber().isEmpty()) {
            user.setPhoneNumber(updateUserDto.getPhoneNumber());
        }


        if (updateUserDto.getBatchName() != null && !updateUserDto.getBatchName().isEmpty()) {
            Optional<BatchModel> batch = batchRepository.findByBatchName(updateUserDto.getBatchName());
            if (batch.isPresent()) {
                user.setBatchId(batch.get().getId());
            } else {
                return ResponseEntity.badRequest().body("Batch not found");
            }
        }

        UsersModel updatedUser = usersRepository.save(user);
        BatchModel batch = batchRepository.findById(updatedUser.getBatchId()).orElse(null);

        UpdateUserResponseDto responseDto = new UpdateUserResponseDto();
        responseDto.setUserId(updatedUser.getUserId());
        responseDto.setName(updatedUser.getName());
        responseDto.setEmail(updatedUser.getEmail());
        responseDto.setBatchId(updatedUser.getBatchId());
        responseDto.setBatchName(batch != null ? batch.getBatchName() : null);
        responseDto.setPhoneNumber(updatedUser.getPhoneNumber());
        responseDto.setBatchType(batch != null ? batch.getBatchType().getBatchType() : null);
        responseDto.setBatchTypeId(batch != null ? batch.getBatchType().getId() : null);

        return ResponseEntity.ok(responseDto);
    }

    private Map<String, Object> validateUserAndBatch(Long userId, InScanDto inScanDto) {
        Map<String, Object> response = new HashMap<>();
        Optional<UsersModel> usersModelOptional = usersRepository.findById(userId);
        if (!usersModelOptional.isPresent()) {
            response.put("message", "Invalid UserId");
            return response;
        }

        UsersModel usersModel = usersModelOptional.get();
        Optional<BatchModel> batchOptional = batchRepository.findById(usersModel.getBatchId());
        if (!batchOptional.isPresent()) {
            response.put("message", "Invalid Batch ID");
            return response;
        }

        BatchModel batch = batchOptional.get();
        Optional<BatchTypeModel> batchTypeOptional = batchTypeRepository.findById(batch.getBatchType().getId());
        if (!batchTypeOptional.isPresent()) {
            response.put("message", "Batch type not found");
            return response;
        }

        LocalDate currentDate = LocalDate.now();
        if (!currentDate.equals(inScanDto.getPresentDate())) {
            response.put("message", "Current date is not correct");
            return response;
        }

        if (!isWithinAllowedDistance(batch.getBatchLatitude(), batch.getBatchLongitude(),
                inScanDto.getUserLatitude(), inScanDto.getUserLongitude(), 5)) {
            response.put("message", "Scan rejected: You are not within the allowed range.");
            return response;
        }

        response.put("user", usersModel);
        response.put("batch", batch);
        response.put("batchType", batchTypeOptional.get());
        return response;
    }

    public ResponseEntity<?> breakScanIn(Long userId, InScanDto inScanDto) {
        Map<String, Object> response = new HashMap<>();
        if (!"in".equalsIgnoreCase(inScanDto.getType())) {
            response.put("message", "Scan type must be 'in' for break scan in");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        Map<String, Object> validationResponse = validateUserAndBatch(userId, inScanDto);
        if (validationResponse.containsKey("message")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationResponse);
        }

        UsersModel usersModel = (UsersModel) validationResponse.get("user");
        BatchModel batch = (BatchModel) validationResponse.get("batch");
        LocalDate currentDate = LocalDate.now();

        List<InOut> breakRecords = inAndOutRepository.findByUserIdAndAttendanceDate(userId, currentDate);
        InOut inOut = breakRecords.isEmpty() ? new InOut() : breakRecords.get(0);

        // Ensure last break scan-in has a corresponding scan-out before allowing new scan-in
        if (inOut.getScanInTime1() != null && inOut.getScanOutTime1() == null) {
            response.put("message", "Scan Out Required Before Next Scan In (Break 1)");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        if (inOut.getScanInTime2() != null && inOut.getScanOutTime2() == null) {
            response.put("message", "Scan Out Required Before Next Scan In (Break 2)");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        if (inOut.getScanInTime3() != null && inOut.getScanOutTime3() == null) {
            response.put("message", "Scan Out Required Before Next Scan In (Break 3)");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // Assigning scan-in time to the correct slot
        inOut.setUserId(userId);
        inOut.setUserName(usersModel.getName());
        inOut.setAttendanceDate(currentDate);
        inOut.setBatchId(usersModel.getBatchId());
        inOut.setBatchName(batch.getBatchName());

        if (inOut.getScanInTime1() == null) {
            inOut.setScanInTime1(inScanDto.getPresentTime());
        } else if (inOut.getScanInTime2() == null) {
            inOut.setScanInTime2(inScanDto.getPresentTime());
        } else if (inOut.getScanInTime3() == null) {
            inOut.setScanInTime3(inScanDto.getPresentTime());
        } else {
            response.put("message", "Maximum Break Scans Reached");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        inAndOutRepository.save(inOut);
        response.put("message", "Break Scan In Recorded Successfully");
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<?> breakScanOut(Long userId, InScanDto inScanDto) {
        Map<String, Object> response = new HashMap<>();
        if (!"out".equalsIgnoreCase(inScanDto.getType())) {
            response.put("message", "Scan type must be 'out' for break scan out");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        Map<String, Object> validationResponse = validateUserAndBatch(userId, inScanDto);
        if (validationResponse.containsKey("message")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationResponse);
        }

        UsersModel usersModel = (UsersModel) validationResponse.get("user");
        LocalDate currentDate = LocalDate.now();

        List<InOut> breakRecords = inAndOutRepository.findByUserIdAndAttendanceDate(userId, currentDate);
        if (breakRecords.isEmpty()) {
            response.put("message", "Invalid scan-out request: No prior scan-in");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        InOut inOut = breakRecords.get(0);

        if (inOut.getScanInTime3() != null && inOut.getScanOutTime3() == null) {
            inOut.setScanOutTime3(inScanDto.getPresentTime());
        } else if (inOut.getScanInTime2() != null && inOut.getScanOutTime2() == null) {
            inOut.setScanOutTime2(inScanDto.getPresentTime());
        } else if (inOut.getScanInTime1() != null && inOut.getScanOutTime1() == null) {
            inOut.setScanOutTime1(inScanDto.getPresentTime());
        } else {
            response.put("message", "Invalid scan-out request: All scan-ins already logged out");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        inAndOutRepository.save(inOut);
        response.put("message", "Break Scan Out Recorded Successfully");
        return ResponseEntity.ok(response);
    }

//    public ResponseEntity<?> breakScanInAndOut(@RequestParam Long userId, @RequestBody InScanDto inScanDto) {
//        Optional<UsersModel> usersModelOptional = usersRepository.findById(userId);
//        if (!usersModelOptional.isPresent()) {
//            return new ResponseEntity<>("Invalid UserId", HttpStatus.NOT_FOUND);
//        }
//
//        UsersModel usersModel = usersModelOptional.get();
//        Optional<BatchModel> batchOptional = batchRepository.findById(usersModel.getBatchId());
//        if (!batchOptional.isPresent()) {
//            return new ResponseEntity<>("Invalid Batch ID", HttpStatus.NOT_FOUND);
//        }
//
//        LocalDate currentDate = LocalDate.now();
//        if (!currentDate.equals(inScanDto.getPresentDate())) {
//            return new ResponseEntity<>("Current date is not correct", HttpStatus.BAD_REQUEST);
//        }
//
//        List<InOut> breakRecords = inAndOutRepository.findByUserIdAndAttendanceDate(userId, currentDate);
//
//
//        if ("in".equalsIgnoreCase(inScanDto.getType())) {
//
//            if (!breakRecords.isEmpty() && breakRecords.get(breakRecords.size() - 1).getScanOutTime1() == null) {
//                return new ResponseEntity<>("Scan Out Required Before Next Scan In", HttpStatus.BAD_REQUEST);
//            }
//
//
//            InOut inOut = new InOut();
//            inOut.setUserId(userId);
//            inOut.setUserName(usersModelOptional.get().getName());
//            inOut.setAttendanceDate(currentDate);
//            inOut.setBatchId(usersModelOptional.get().getBatchId());
//            inOut.setBatchName(batchOptional.get().getBatchName());
//
//            if (breakRecords.isEmpty() || breakRecords.get(0).getScanInTime1() == null) {
//                inOut.setScanInTime1(inScanDto.getPresentTime());
//            } else if (breakRecords.get(0).getScanInTime2() == null) {
//                inOut = breakRecords.get(0);
//                inOut.setScanInTime2(inScanDto.getPresentTime());
//            } else if (breakRecords.get(0).getScanInTime3() == null) {
//                inOut = breakRecords.get(0);
//                inOut.setScanInTime3(inScanDto.getPresentTime());
//            } else {
//                return new ResponseEntity<>("Maximum Break Scans Reached", HttpStatus.BAD_REQUEST);
//            }
//
//            inAndOutRepository.save(inOut);
//            return new ResponseEntity<>("Break Scan In Recorded Successfully", HttpStatus.OK);
//        }
//
//
//        else if ("out".equalsIgnoreCase(inScanDto.getType())) {
//            if (breakRecords.isEmpty()) {
//                return new ResponseEntity<>("Invalid scan-out request: No prior scan-in", HttpStatus.BAD_REQUEST);
//            }
//
//            InOut inOut = breakRecords.get(0);
//
//            if (inOut.getScanInTime3() != null && inOut.getScanOutTime3() == null) {
//                inOut.setScanOutTime3(inScanDto.getPresentTime());
//            } else if (inOut.getScanInTime2() != null && inOut.getScanOutTime2() == null) {
//                inOut.setScanOutTime2(inScanDto.getPresentTime());
//            } else if (inOut.getScanInTime1() != null && inOut.getScanOutTime1() == null) {
//                inOut.setScanOutTime1(inScanDto.getPresentTime());
//            } else {
//                return new ResponseEntity<>("Invalid scan-out request: All scan-ins already logged out", HttpStatus.BAD_REQUEST);
//            }
//
//            inAndOutRepository.save(inOut);
//            return new ResponseEntity<>("Break Scan Out Recorded Successfully", HttpStatus.OK);
//        }
//
//        return new ResponseEntity<>("Scan Type is not mentioned", HttpStatus.BAD_REQUEST);
//    }
}

