package com.example.ptfAttendanceSystem.model;



import com.example.ptfAttendanceSystem.attendance.Attendance;
import com.example.ptfAttendanceSystem.attendance.AttendanceRepository;
import com.example.ptfAttendanceSystem.late.LateRequestModel;
import com.example.ptfAttendanceSystem.late.LateRequestRepository;
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
//    public ResponseEntity<?> scanInAndOut(Long userId, InScanDto inScanDto) {
//        Optional<UsersModel> usersModelOptional = usersRepository.findById(userId);
//        if (usersModelOptional.isPresent()) {
//            UsersModel usersModel = usersModelOptional.get();
//            String batchType = usersModel.getBatch();
//            String typeData = inScanDto.getType();
//            if (typeData!=null&&typeData.equalsIgnoreCase("in")) {
//                LocalDate currentDate = LocalDate.now();
//                if (currentDate.equals(inScanDto.getPresentDate())) {
//                    if (batchType.equalsIgnoreCase("morning batch")) {
//                        LocalTime allowedTime = LocalTime.of(19, 41);
//                        if (allowedTime.isBefore(inScanDto.getPresentTime())) {
//                            Attendance attendance = new Attendance();
//                            attendance.setAttendanceDate(inScanDto.getPresentDate());
//                            attendance.setUserId(userId);
//                            attendance.setScanInTime(inScanDto.getPresentTime());
//                            attendance.setStatus("Punctual");
//                            attendanceRepository.save(attendance);
//                            return new ResponseEntity<>(attendance, HttpStatus.OK);
//
//                        } else {
//                            Optional<LateRequestModel> lateRequestModelOptional = lateRequestRepository.findByUserIdAndDate(userId, inScanDto.getPresentDate());
//                            if (lateRequestModelOptional.isPresent()) {
//                                Attendance attendance = new Attendance();
//                                attendance.setScanInTime(inScanDto.getPresentTime());
//                                attendance.setAttendanceDate(inScanDto.getPresentDate());
//                                attendance.setUserId(userId);
//                                attendance.setStatus("Punctual");
//                                attendanceRepository.save(attendance);
//                                return new ResponseEntity<>("Attendance marked", HttpStatus.OK);
//                            }else {
//                                Attendance attendance = new Attendance();
//                                attendance.setUserId(userId);
//                                attendance.setAttendanceDate(inScanDto.getPresentDate());
//                                attendance.setScanInTime(inScanDto.getPresentTime());
//                                attendance.setStatus("Late");
//                                attendanceRepository.save(attendance);
//                                return new ResponseEntity<>("Late Attendance Marked",HttpStatus.OK);
//                            }
//                        }
//                    } else if (batchType.equalsIgnoreCase("evening batch")) {
//                        LocalTime allowedTime = LocalTime.of(13, 41);
//                        if (allowedTime.isBefore(inScanDto.getPresentTime())) {
//                            Attendance attendance = new Attendance();
//                            attendance.setScanInTime(inScanDto.getPresentTime());
//                            attendance.setAttendanceDate(inScanDto.getPresentDate());
//                            attendance.setUserId(userId);
//                            attendance.setStatus("Punctual");
//                            attendanceRepository.save(attendance);
//                            return new ResponseEntity<>(attendance, HttpStatus.OK);
//                        }else {
//                            Optional<LateRequestModel> lateRequestModelOptional = lateRequestRepository.findByUserIdAndDate(userId, inScanDto.getPresentDate());
//                            if (lateRequestModelOptional.isPresent()) {
//                                Attendance attendance = new Attendance();
//                                attendance.setScanInTime(inScanDto.getPresentTime());
//                                attendance.setAttendanceDate(inScanDto.getPresentDate());
//                                attendance.setUserId(userId);
//                                attendance.setStatus("Punctual");
//                                attendanceRepository.save(attendance);
//                                return new ResponseEntity<>("Attendance marked", HttpStatus.OK);
//                            }else {
//                                Attendance attendance = new Attendance();
//                                attendance.setUserId(userId);
//                                attendance.setAttendanceDate(inScanDto.getPresentDate());
//                                attendance.setScanInTime(inScanDto.getPresentTime());
//                                attendance.setStatus("Late");
//                                attendanceRepository.save(attendance);
//                                return new ResponseEntity<>("Late Attendance Marked",HttpStatus.OK);
//                            }
//                        }
//                    } else {
//                        return new ResponseEntity<>("Batch type is n't valid", HttpStatus.NOT_FOUND);
//                    }
//                }return new ResponseEntity<>("Current date is not correct",HttpStatus.BAD_REQUEST);
//            }else if (typeData!=null &&typeData.equalsIgnoreCase("out")) {
//                LocalDate currentDate = LocalDate.now();
//                if (currentDate.equals(inScanDto.getPresentDate())) {
//                    Optional<Attendance> attendanceOptional = attendanceRepository.findByUserId(userId);
//                    if (attendanceOptional.isPresent()) {
//                        Attendance attendance = attendanceOptional.get();
//                        attendance.setScanOutTime(inScanDto.getPresentTime());
//                        attendanceRepository.save(attendance);
//                        return new ResponseEntity<>("Scan Out Time : " + inScanDto.getPresentTime(), HttpStatus.OK);
//                    } else {
//                        return new ResponseEntity<>("userId is not valid", HttpStatus.NOT_FOUND);
//                    }
//                } else {
//                    return new ResponseEntity<>("Scan out Date is not present", HttpStatus.NOT_FOUND);
//                }
//            } else {
//                return new ResponseEntity<>("Scan Type is not mentioned", HttpStatus.NOT_FOUND);
//            }
//
//        }
//        return new ResponseEntity<>("Invalid UserId ", HttpStatus.NOT_FOUND);
//    }


    public ResponseEntity<?> scanInAndOut(Long userId, InScanDto inScanDto) {
        Optional<UsersModel> usersModelOptional = usersRepository.findById(userId);
        if (usersModelOptional.isPresent()) {
            UsersModel usersModel = usersModelOptional.get();

            // Ensure userName and batchType are retrieved
            String userName = usersModel.getName(); // Ensure `getName` is correct
            String batchType = usersModel.getBatch(); // Ensure `getBatch` is correct

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
        if (currentDate.equals(inScanDto.getPresentDate())) {
            Attendance attendance = new Attendance();
            attendance.setUserId(userId);
            attendance.setUserName(userName);
            attendance.setBatchType(batchType);
            attendance.setAttendanceDate(inScanDto.getPresentDate());
            attendance.setScanInTime(inScanDto.getPresentTime());

            if (batchType.equalsIgnoreCase("morning batch")) {
                LocalTime allowedTime = LocalTime.of(9, 41);
                attendance.setStatus(inScanDto.getPresentTime().isBefore(allowedTime) ? "Punctual" : "Late");
            } else if (batchType.equalsIgnoreCase("evening batch")) {
                LocalTime allowedTime = LocalTime.of(13, 41);
                attendance.setStatus(inScanDto.getPresentTime().isBefore(allowedTime) ? "Punctual" : "Late");
            } else {
                return new ResponseEntity<>("Batch type isn't valid", HttpStatus.NOT_FOUND);
            }

            attendanceRepository.save(attendance);
            return new ResponseEntity<>("Scan In Successful", HttpStatus.OK);
        }
        return new ResponseEntity<>("Current date is not correct", HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<?> handleScanOut(Long userId, InScanDto inScanDto) {
        LocalDate currentDate = LocalDate.now();
        if (currentDate.equals(inScanDto.getPresentDate())) {
            Optional<Attendance> attendanceOptional = attendanceRepository.findByUserIdAndAttendanceDate(userId, currentDate);
            if (attendanceOptional.isPresent()) {
                Attendance attendance = attendanceOptional.get();
                attendance.setScanOutTime(inScanDto.getPresentTime());
                attendanceRepository.save(attendance);
                return new ResponseEntity<>("Scan Out Successful", HttpStatus.OK);
            }
            return new ResponseEntity<>("No Scan In record found for the user", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>("Scan Out Date is not correct", HttpStatus.BAD_REQUEST);
    }





    public List<Attendance> getAllAttendanceByDate(LocalDate date) {
        // Retrieve all attendance records for the given date
        return attendanceRepository.findByAttendanceDate(date);
    }



    public List<Attendance> getAttendanceForMonth(Long userId, int month, int year) {
        // Create a date range for the given month and year
        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());

        // Retrieve all attendance records for the user in the given month
        return attendanceRepository.findByUserIdAndAttendanceDateBetween(userId, startOfMonth, endOfMonth);
    }

    public Optional<UsersModel> findUserByToken(String token) {
        // Here you should implement the logic to retrieve the user associated with the token
        return usersRepository.findByToken(token); // You need to implement this in the repository
    }






    public List<LeaveRequestModel> getLeaveRequestsByUserId(Long userId) {
        return leaveRequestRepository.findByUserId(userId); // Ensure this method is defined in the repository
    }




//    public Optional<UsersModel> findByEmail(String email) {
//        return usersRepository.findByEmail(email); // Ensure this method exists in your repository
//    }


    public Optional<UsersModel> findByEmailAndPassword(String email, String password) {
        return usersRepository.findByEmailAndPassword(email, password);
    }


    public void updateUserToken(UsersModel usersModel) {
        usersRepository.save(usersModel); // Save the updated admin model with the token
    }

    public List<UsersModel> getAllUsers() {
        return usersRepository.findAll();
    }

    public Optional<UsersModel> getUserById(Long id) {
        return usersRepository.findById(id);
    }
//
//    //27/7/24
//    public UsersModel updateUserPassword(Long id, UserDto userDto) throws Exception {
//        Optional<UsersModel> existingUser = usersRepository.findById(id);
//        if (!existingUser.isPresent()) {
//            throw new Exception("User Not Found");
//        }
//        UsersModel user = existingUser.get();
//        user.setPassword(userDto.getPassword());
//        return usersRepository.save(user);
//    }
//




    public List<LateRequestModel> getLateRequestsByUserId(Long userId) {
        return lateRequestRepository.findByUserId(userId);
    }

//
//    public ResponseEntity<?> createLateRequest(LateRequestDto lateRequestDto) {
//        Optional<UsersModel> usersModelOptional = usersRepository.findById(lateRequestDto.getUserId());
//        if (usersModelOptional.isPresent()){
//            UsersModel usersModel = usersModelOptional.get();
//            if (usersModel.getBatch().equalsIgnoreCase("Morning batch")){
//                LocalTime lateMaxTime = LocalTime.of(9,40);
//                if (LocalTime.now().isBefore(lateMaxTime)){
//                    Optional<LateRequestModel> lateRequestModelOptional = lateRequestRepository.findByUserIdAndDate(lateRequestDto.getUserId(),lateRequestDto.getDate());
//                    if (lateRequestModelOptional.isPresent()){
//                        return new ResponseEntity<>("Late request already Submitted",HttpStatus.BAD_REQUEST);
//                    }else {
//                        LateRequestModel requestModel = new LateRequestModel();
//                        requestModel.setUserId(lateRequestDto.getUserId());
//                        requestModel.setDate(LocalDate.now());
//                        requestModel.setReason(lateRequestDto.getReason());
//                        requestModel.setStatus(LateRequestStatus.PENDING);
//                        lateRequestRepository.save(requestModel);
//                        return new ResponseEntity<>("Late request marked ",HttpStatus.OK);
//                    }
//
//                }else {
//                    return new ResponseEntity<>("Late request is not allowed for this current time ",HttpStatus.CONFLICT);
//                }
//            } else if (usersModel.getBatch().equalsIgnoreCase("Evening Batch")) {
//                LocalTime lateMaxTime = LocalTime.of(13,40);
//                if (LocalTime.now().isBefore(lateMaxTime)){
//                    Optional<LateRequestModel> lateRequestModelOptional = lateRequestRepository.findByUserIdAndDate(lateRequestDto.getUserId(),lateRequestDto.getDate());
//                    if (lateRequestModelOptional.isPresent()){
//                        return new ResponseEntity<>("Late request already Submitted",HttpStatus.BAD_REQUEST);
//                    }else {
//                        LateRequestModel requestModel = new LateRequestModel();
//                        requestModel.setUserId(lateRequestDto.getUserId());
//                        requestModel.setDate(LocalDate.now());
//                        requestModel.setReason(lateRequestDto.getReason());
//                        requestModel.setStatus(LateRequestStatus.PENDING);
//                        lateRequestRepository.save(requestModel);
//                        return new ResponseEntity<>("Late request marked ",HttpStatus.OK);
//                    }
//
//                }else {
//                    return new ResponseEntity<>("Late request is not allowed for this current time ",HttpStatus.CONFLICT);
//                }
//            }
//
//        }return new ResponseEntity<>("UserId is not valid",HttpStatus.NOT_FOUND);
//    }
////

    public Optional<UsersModel> findByEmail(String email) {
        return usersRepository.findByEmail(email); // Ensure this method exists in your repository
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

