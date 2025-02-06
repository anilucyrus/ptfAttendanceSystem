package com.example.ptfAttendanceSystem.leave;


import com.example.ptfAttendanceSystem.model.UsersModel;
import com.example.ptfAttendanceSystem.model.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final UsersService usersService;

    @Autowired
    public LeaveRequestService(LeaveRequestRepository leaveRequestRepository, UsersService usersService) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.usersService = usersService;
    }

    public ResponseEntity<?> requestLeave(Long userId, LeaveRequestDto leaveRequestDto) {
        LocalDate currentDate = LocalDate.now();

        if (leaveRequestDto.getFromDate().isBefore(currentDate) || leaveRequestDto.getToDate().isBefore(currentDate)) {
            return ResponseEntity.badRequest().body("Leave request cannot be made for past dates.");
        }

        if (leaveRequestDto.getFromDate().isAfter(leaveRequestDto.getToDate())) {
            return ResponseEntity.badRequest().body("From date cannot be after To date.");
        }

        Optional<UsersModel> userOptional = usersService.getUserById(userId);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }

        if (leaveRequestRepository.existsByUserIdAndFromDateLessThanEqualAndToDateGreaterThanEqual(userId, leaveRequestDto.getToDate(), leaveRequestDto.getFromDate())) {
            return ResponseEntity.badRequest().body("User already has a leave request for the selected date(s).");
        }

        UsersModel user = userOptional.get();
        LeaveRequestModel leaveRequest = new LeaveRequestModel();
        leaveRequest.setUserId(user.getUserId());
        leaveRequest.setLeaveType(leaveRequestDto.getLeaveType());
        leaveRequest.setReason(leaveRequestDto.getReason());
        leaveRequest.setFromDate(leaveRequestDto.getFromDate());
        leaveRequest.setToDate(leaveRequestDto.getToDate());
        leaveRequest.setNumberOfDays(Period.between(leaveRequestDto.getFromDate(), leaveRequestDto.getToDate()).getDays() + 1);
        leaveRequest.setStatus(LeaveRequestStatus.PENDING);
        leaveRequest.setName(user.getName());
        leaveRequest.setBatchId(user.getBatchId());

        leaveRequestRepository.save(leaveRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(LeaveRequestMapper.toResponseDto(leaveRequest));
    }

    public ResponseEntity<?> updateLeaveRequest(Long requestId, LeaveRequestDto leaveRequestDto) {
        LocalDate currentDate = LocalDate.now();

        if (leaveRequestDto.getFromDate().isBefore(currentDate) || leaveRequestDto.getToDate().isBefore(currentDate)) {
            return ResponseEntity.badRequest().body("Leave request cannot be made for past dates.");
        }

        if (leaveRequestDto.getFromDate().isAfter(leaveRequestDto.getToDate())) {
            return ResponseEntity.badRequest().body("From date cannot be after To date.");
        }

        Optional<LeaveRequestModel> leaveRequestOptional = leaveRequestRepository.findById(requestId);
        if (leaveRequestOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Leave request not found.");
        }

        LeaveRequestModel leaveRequest = leaveRequestOptional.get();
        leaveRequest.setLeaveType(leaveRequestDto.getLeaveType());
        leaveRequest.setReason(leaveRequestDto.getReason());
        leaveRequest.setFromDate(leaveRequestDto.getFromDate());
        leaveRequest.setToDate(leaveRequestDto.getToDate());
        leaveRequest.setNumberOfDays(Period.between(leaveRequestDto.getFromDate(), leaveRequestDto.getToDate()).getDays() + 1);
        leaveRequest.setStatus(LeaveRequestStatus.PENDING);

        leaveRequestRepository.save(leaveRequest);

        return ResponseEntity.ok(LeaveRequestMapper.toResponseDto(leaveRequest));
    }


    public ResponseEntity<?> getAllLeaveRequestsByUserId(Long userId) {
        List<LeaveRequestModel> leaveRequests = leaveRequestRepository.findByUserId(userId);
        if (leaveRequests.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No leave requests found for the user.");
        }
        List<LeaveRequestResponseDto> responseDtos = leaveRequests.stream()
                .map(LeaveRequestMapper::toResponseDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDtos);
    }


    public ResponseEntity<?> deleteLeaveRequest(Long requestId) {
        Optional<LeaveRequestModel> leaveRequestOptional = leaveRequestRepository.findById(requestId);
        if (leaveRequestOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Leave request not found.");
        }
        leaveRequestRepository.deleteById(requestId);
        return ResponseEntity.status(HttpStatus.OK).body("Leave request deleted successfully.");
    }

}

