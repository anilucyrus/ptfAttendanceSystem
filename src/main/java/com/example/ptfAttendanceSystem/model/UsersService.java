package com.example.ptfAttendanceSystem.model;




import com.example.ptfAttendanceSystem.attendance.Attendance;
import com.example.ptfAttendanceSystem.attendance.AttendanceRepository;
import com.example.ptfAttendanceSystem.batch.BatchModel;
import com.example.ptfAttendanceSystem.batch.BatchRepository;
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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

        // Fetch batch details
        BatchModel batch = batchRepository.findById(savedUser.getBatchId()).orElse(null);

        // Create response
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

            // Fetch batch details
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

    public ResponseEntity<?> scanInAndOut(Long userId, InScanDto inScanDto) {
        Optional<UsersModel> usersModelOptional = usersRepository.findById(userId);
        if (!usersModelOptional.isPresent()) {
            return new ResponseEntity<>("Invalid UserId", HttpStatus.NOT_FOUND);
        }

        UsersModel usersModel = usersModelOptional.get();
        Optional<BatchModel> batchOptional = batchRepository.findById(usersModel.getBatchId());

        if (!batchOptional.isPresent()) {
            return new ResponseEntity<>("Invalid Batch ID", HttpStatus.NOT_FOUND);
        }

        BatchModel batch = batchOptional.get();
        String typeData = inScanDto.getType();

        try {
            if ("in".equalsIgnoreCase(typeData)) {
                return handleScanIn(userId, usersModel.getName(), batch, inScanDto);
            } else if ("out".equalsIgnoreCase(typeData)) {
                return handleScanOut(userId, inScanDto);
            } else {
                return new ResponseEntity<>("Scan Type is not mentioned", HttpStatus.BAD_REQUEST);
            }
        } finally {
            qrCodeService.regenerateQRCode();
        }
    }

    private ResponseEntity<?> handleScanIn(Long userId, String userName, BatchModel batch, InScanDto inScanDto) {
        LocalDate currentDate = LocalDate.now();
        if (!currentDate.equals(inScanDto.getPresentDate())) {
            return new ResponseEntity<>("Current date is not correct", HttpStatus.BAD_REQUEST);
        }


        Optional<Attendance> existingAttendance = attendanceRepository.findByUserIdAndAttendanceDate(userId, currentDate);
        if (existingAttendance.isPresent() && existingAttendance.get().getScanInTime() != null) {
            return new ResponseEntity<>("User has already scanned in today", HttpStatus.BAD_REQUEST);
        }


        Optional<LateRequestModel> lateRequestOptional = lateRequestRepository.findByUserIdAndDate(userId, currentDate);
        if (lateRequestOptional.isPresent() && lateRequestOptional.get().getStatus() == LateRequestStatus.APPROVED) {
            return markApprovedLateAttendance(userId, userName, batch, currentDate);
        }


        List<LateAttendance> lateRecords = lateAttendanceRepository.findByUserId(userId);
        if (lateRecords.size() >= 4) {
            lateAttendanceRepository.deleteAll(lateRecords);
            return new ResponseEntity<>("Attendance not marked. You have been marked as leave due to 4 late attendances.", HttpStatus.BAD_REQUEST);
        }

        LocalTime allowedTime = batch.getStartTime().plusMinutes(10);
        boolean isLate = inScanDto.getPresentTime().isAfter(allowedTime);

        Attendance attendance = new Attendance();
        attendance.setUserId(userId);
        attendance.setUserName(userName);
        attendance.setBatchType(batch.getBatchName());
        attendance.setAttendanceDate(currentDate);
        attendance.setScanInTime(inScanDto.getPresentTime());
        attendance.setStatus(isLate ? "Late" : "Punctual");
        attendanceRepository.save(attendance);

        if (isLate) {
            saveLateUser(userId, userName, batch.getBatchName(), inScanDto, "Late");
        }

        return new ResponseEntity<>(isLate ? "Late Scan In Recorded" : "Scan In Successful", HttpStatus.OK);
    }

    private ResponseEntity<?> markApprovedLateAttendance(Long userId, String userName, BatchModel batch, LocalDate currentDate) {
        LocalTime allowedTime = batch.getStartTime().plusMinutes(30);

        Attendance attendance = new Attendance();
        attendance.setUserId(userId);
        attendance.setUserName(userName);
        attendance.setBatchType(batch.getBatchName());
        attendance.setAttendanceDate(currentDate);
        attendance.setScanInTime(allowedTime);
        attendance.setStatus("Punctual");

        attendanceRepository.save(attendance);
        return new ResponseEntity<>("Scan In Successful (Late Request Approved)", HttpStatus.OK);
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

    private ResponseEntity<?> handleScanOut(Long userId, InScanDto inScanDto) {
        LocalDate currentDate = LocalDate.now();
        if (!currentDate.equals(inScanDto.getPresentDate())) {
            return new ResponseEntity<>("Scan Out Date is not correct", HttpStatus.BAD_REQUEST);
        }

        Optional<Attendance> attendanceOptional = attendanceRepository.findByUserIdAndAttendanceDate(userId, currentDate);
        if (!attendanceOptional.isPresent() || attendanceOptional.get().getScanInTime() == null) {
            return new ResponseEntity<>("User must scan in before scanning out", HttpStatus.BAD_REQUEST);
        }

        Attendance attendance = attendanceOptional.get();
        attendance.setScanOutTime(inScanDto.getPresentTime());
        attendanceRepository.save(attendance);

        return new ResponseEntity<>("Scan Out Successful", HttpStatus.OK);
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

    public List<UsersModel> getAllUsers() {
        return usersRepository.findAll();
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

}

