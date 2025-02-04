package com.example.ptfAttendanceSystem.model;


import com.example.ptfAttendanceSystem.attendance.Attendance;
import com.example.ptfAttendanceSystem.attendance.AttendanceRepository;
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
    private UsersRepository usersRepository;

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




    @Autowired

    public ResponseEntity<?> userRegistration(UserDto userDto) {
        Optional<UsersModel> existingUser = usersRepository.findByEmail(userDto.getEmail());
        if (existingUser.isPresent()) {
            return new ResponseEntity<>("User already registered", HttpStatus.CONFLICT);
        }

        UsersModel user = new UsersModel();
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        user.setPassword(userDto.getPassword());
        user.setBatch(userDto.getBatch());
        user.setPhoneNumber(userDto.getPhoneNumber());
        UsersModel savedUser = usersRepository.save(user);

        if (!savedUser.getEmail().isEmpty()){
            sendRegistrationEmail(savedUser.getEmail());
        }else {
            return new ResponseEntity<>("Email is required",HttpStatus.BAD_REQUEST);
        }

        URegistrationResponse responce = new URegistrationResponse(
                savedUser.getUserId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getBatch(),
                savedUser.getPhoneNumber()
        );

        return new ResponseEntity<>(responce, HttpStatus.CREATED);
    }

    private void sendRegistrationEmail(String toEmail) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Registration Confirmation");
        message.setText("Thank you for registering PTF application!");

        mailSender.send(message);
    }

    public ResponseEntity<?> scanInAndOut(Long userId, InScanDto inScanDto) {
        Optional<UsersModel> usersModelOptional = usersRepository.findById(userId);
        if (usersModelOptional.isPresent()) {
            UsersModel usersModel = usersModelOptional.get();
            String userName = usersModel.getName();
            String batchType = usersModel.getBatch();

            String typeData = inScanDto.getType();

            try {
                if (typeData != null && typeData.equalsIgnoreCase("in")) {
                    return handleScanIn(userId, userName, batchType, inScanDto);
                } else if (typeData != null && typeData.equalsIgnoreCase("out")) {
                    return handleScanOut(userId, inScanDto);
                } else {
                    return new ResponseEntity<>("Scan Type is not mentioned", HttpStatus.BAD_REQUEST);
                }
            } finally {
                qrCodeService.regenerateQRCode();
            }
        }
        return new ResponseEntity<>("Invalid UserId", HttpStatus.NOT_FOUND);
    }



    private ResponseEntity<?> handleScanIn(Long userId, String userName, String batchType, InScanDto inScanDto) {
        if (userName == null || batchType == null) {
            return new ResponseEntity<>("User details are incomplete", HttpStatus.BAD_REQUEST);
        }

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
            LocalTime allowedTime = batchType.equalsIgnoreCase("morning batch") ? LocalTime.of(9, 30) : LocalTime.of(13, 30);

            Attendance attendance = new Attendance();
            attendance.setUserId(userId);
            attendance.setUserName(userName);
            attendance.setBatchType(batchType);
            attendance.setAttendanceDate(currentDate);
            attendance.setScanInTime(allowedTime);
            attendance.setStatus("Punctual");

            attendanceRepository.save(attendance);
            return new ResponseEntity<>("Scan In Successful (Late Request Approved)", HttpStatus.OK);
        }

        List<LateAttendance> lateRecords = lateAttendanceRepository.findByUserId(userId);
        if (lateRecords.size() >= 4) {
            lateAttendanceRepository.deleteAll(lateRecords);
            return new ResponseEntity<>("Attendance not marked. You have been marked as leave due to 4 late attendances.", HttpStatus.BAD_REQUEST);
        }

        Attendance attendance = new Attendance();
        attendance.setUserId(userId);
        attendance.setUserName(userName);
        attendance.setBatchType(batchType);
        attendance.setAttendanceDate(currentDate);
        attendance.setScanInTime(inScanDto.getPresentTime());

        if (batchType.equalsIgnoreCase("morning batch")) {
            LocalTime allowedTime = LocalTime.of(9, 40);
            if (inScanDto.getPresentTime().isAfter(allowedTime)) {
                saveLateUser(userId, userName, batchType, inScanDto, "Late");
                attendance.setStatus("Late");
            } else {
                attendance.setStatus("Punctual");
            }
        } else if (batchType.equalsIgnoreCase("evening batch")) {
            LocalTime allowedTime = LocalTime.of(13, 40);
            if (inScanDto.getPresentTime().isAfter(allowedTime)) {
                saveLateUser(userId, userName, batchType, inScanDto, "Late");
                attendance.setStatus("Late");
            } else {
                attendance.setStatus("Punctual");
            }
        }

        else if (batchType.equalsIgnoreCase("regular batch")) {
            LocalTime allowedTime = LocalTime.of(9, 30);
            if (inScanDto.getPresentTime().isAfter(allowedTime)) {
                saveLateUser(userId, userName, batchType, inScanDto, "Late");
                attendance.setStatus("Late");
            }

            else {
                attendance.setStatus("Punctual");
            }
        } else {
            return new ResponseEntity<>("Batch type isn't valid", HttpStatus.NOT_FOUND);
        }

        attendanceRepository.save(attendance);
        return new ResponseEntity<>("Scan In Successful", HttpStatus.OK);
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
        if (currentDate.equals(inScanDto.getPresentDate())) {
            Optional<Attendance> attendanceOptional = attendanceRepository.findByUserIdAndAttendanceDate(userId, currentDate);
            if (!attendanceOptional.isPresent() || attendanceOptional.get().getScanInTime() == null) {
                return new ResponseEntity<>("User must scan in before scanning out", HttpStatus.BAD_REQUEST);
            }

            Attendance attendance = attendanceOptional.get();
            attendance.setScanOutTime(inScanDto.getPresentTime());
            attendanceRepository.save(attendance);
            return new ResponseEntity<>("Scan Out Successful", HttpStatus.OK);
        }
        return new ResponseEntity<>("Scan Out Date is not correct", HttpStatus.BAD_REQUEST);
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

    public ResponseEntity<?> updateUser(Long id, UserDto userDto) {
        Optional<UsersModel> userOptional = usersRepository.findById(id);
        if (userOptional.isPresent()) {
            UsersModel user = userOptional.get();
            user.setName(userDto.getName());
            user.setEmail(userDto.getEmail());
            user.setPassword(userDto.getPassword());
            user.setBatch(userDto.getBatch());
            user.setPhoneNumber(userDto.getPhoneNumber());
            UsersModel updatedUser = usersRepository.save(user);

            URegistrationResponse response = new URegistrationResponse(
                    updatedUser.getUserId(),
                    updatedUser.getName(),
                    updatedUser.getEmail(),
                    updatedUser.getBatch(),
                    updatedUser.getPhoneNumber()
            );

            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }
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

