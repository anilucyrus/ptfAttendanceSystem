package com.example.ptfAttendanceSystem.leave;


import com.example.ptfAttendanceSystem.batch.BatchModel;
import com.example.ptfAttendanceSystem.batch.BatchRepository;
import com.example.ptfAttendanceSystem.batchType.BatchTypeModel;
import com.example.ptfAttendanceSystem.batchType.BatchTypeRepository;
import com.example.ptfAttendanceSystem.leaveAndWFH.*;
import com.example.ptfAttendanceSystem.model.UsersModel;
import com.example.ptfAttendanceSystem.model.UsersRepository;
import com.example.ptfAttendanceSystem.model.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LeaveRequestService {
    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private BatchRepository batchRepository;
    @Autowired
    private BatchTypeRepository batchTypeRepository;
    @Autowired
    private WfhRepo wfhRepo;

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

//    public ResponseEntity<?> updateLeaveRequest(Long requestId, LeaveRequestDto leaveRequestDto) {
//        LocalDate currentDate = LocalDate.now();
//
//        if (leaveRequestDto.getFromDate().isBefore(currentDate) || leaveRequestDto.getToDate().isBefore(currentDate)) {
//            return ResponseEntity.badRequest().body("Leave request cannot be made for past dates.");
//        }
//
//        if (leaveRequestDto.getFromDate().isAfter(leaveRequestDto.getToDate())) {
//            return ResponseEntity.badRequest().body("From date cannot be after To date.");
//        }
//
//        Optional<LeaveRequestModel> leaveRequestOptional = leaveRequestRepository.findById(requestId);
//        if (leaveRequestOptional.isEmpty()) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Leave request not found.");
//        }
//
//        LeaveRequestModel leaveRequest = leaveRequestOptional.get();
//        leaveRequest.setLeaveType(leaveRequestDto.getLeaveType());
//        leaveRequest.setReason(leaveRequestDto.getReason());
//        leaveRequest.setFromDate(leaveRequestDto.getFromDate());
//        leaveRequest.setToDate(leaveRequestDto.getToDate());
//        leaveRequest.setNumberOfDays(Period.between(leaveRequestDto.getFromDate(), leaveRequestDto.getToDate()).getDays() + 1);
//        leaveRequest.setStatus(LeaveRequestStatus.PENDING);
//
//        leaveRequestRepository.save(leaveRequest);
//
//        return ResponseEntity.ok(LeaveRequestMapper.toResponseDto(leaveRequest));
//    }


    public ResponseEntity<?> getAllLeaveRequestsByUserId(Long userId) {
        List<LeaveRequestModel> leaveRequests = leaveRequestRepository.findByUserId(userId);

        Optional<UsersModel> usersModelOptional = usersRepository.findById(userId);
        if (usersModelOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid User ID.");
        }
        if (leaveRequests.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No leave requests found for the user.");
        }


        List<Map<String, Object>> responseList = leaveRequests.stream().map(leave -> {
            Map<String, Object> response = new HashMap<>();
            response.put("id", leave.getId());
            response.put("userId", leave.getUserId());
            response.put("leaveType", leave.getLeaveType());
            response.put("reason", leave.getReason());
            response.put("name", leave.getName());
            response.put("fromDate", leave.getFromDate());
            response.put("toDate", leave.getToDate());
            response.put("numberOfDays", leave.getNumberOfDays());
            response.put("status", leave.getStatus());

            // Fetch batch details
            Optional<BatchModel> batchOptional = batchRepository.findById(leave.getBatchId());
            if (batchOptional.isPresent()) {
                response.put("batchId", batchOptional.get().getId());
                response.put("batchName", batchOptional.get().getBatchName());
            } else {
                response.put("batchId", leave.getBatchId());
                response.put("batchName", "Unknown Batch");
            }

            return response;
        }).collect(Collectors.toList());

        return new ResponseEntity<>(responseList, HttpStatus.OK);
    }


    public ResponseEntity<?> getAllWorkFromHomeRequestsByUserId(Long userId) {
        List<Wfh> wfhList = wfhRepo.findByUserId(userId);
        List<LeaveRequestResponseDto> leaveRequestResponseDtos = new ArrayList<>();
        if (!wfhList.isEmpty()) {
            for (Wfh wfh : wfhList) {
                LeaveRequestResponseDto leaveRequestResponseDto = new LeaveRequestResponseDto();
                leaveRequestResponseDto.setId(wfh.getId());
                leaveRequestResponseDto.setUserId(wfh.getUserId());

                Optional<UsersModel> usersModelOptional = usersRepository.findById(wfh.getUserId());
                if (usersModelOptional.isPresent()) {
                    UsersModel usersModel = usersModelOptional.get();
                    leaveRequestResponseDto.setName(usersModel.getName());
                }
                leaveRequestResponseDto.setBatchId(wfh.getBatchId());
                leaveRequestResponseDto.setLeaveType(wfh.getLeaveType());
                leaveRequestResponseDto.setReason(wfh.getReason());
                leaveRequestResponseDto.setFromDate(wfh.getFromDate());
                leaveRequestResponseDto.setToDate(wfh.getToDate());
                leaveRequestResponseDto.setNumberOfDays(wfh.getNumberOfDays());
//                leaveRequestResponseDto.setStatus();
                leaveRequestResponseDto.setStatus(LeaveRequestStatus.valueOf(wfh.getStatus().name()));
                leaveRequestResponseDtos.add(leaveRequestResponseDto);
            }
            return new ResponseEntity<>(leaveRequestResponseDtos, HttpStatus.OK);
        }
        return new ResponseEntity<>("No data found", HttpStatus.NO_CONTENT);
    }


    public ResponseEntity<?> deleteLeaveRequest(Long requestId) {
        Optional<LeaveRequestModel> leaveRequestOptional = leaveRequestRepository.findById(requestId);
        if (leaveRequestOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Leave request not found.");
        }
        leaveRequestRepository.deleteById(requestId);
        return ResponseEntity.status(HttpStatus.OK).body("Leave request deleted successfully.");
    }

    public ResponseEntity<?> requestWorkFromHome(Long userId, LeaveRequestDto leaveRequestDto) {
        Optional<UsersModel> usersModelOptional = usersRepository.findById(userId);


        if (usersModelOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid User ID.");
        }

        UsersModel usersModel = usersModelOptional.get();
        Optional<BatchModel> batchOptional = batchRepository.findById(usersModel.getBatchId());
        if (batchOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid Batch ID.");
        }


        BatchModel batch = batchOptional.get();
        Optional<BatchTypeModel> batchTypeOptional = batchTypeRepository.findById(batch.getBatchType().getId());

        LocalDate currentDate = LocalDate.now();
        if (leaveRequestDto.getFromDate().isBefore(currentDate) || leaveRequestDto.getToDate().isBefore(currentDate)) {
            return ResponseEntity.badRequest().body("Leave request cannot be made for past dates.");
        }

        if (leaveRequestDto.getFromDate().isAfter(leaveRequestDto.getToDate())) {
            return ResponseEntity.badRequest().body("From date cannot be after To date.");
        }
        if (batchTypeOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Batch type not found.");
        }



        BatchTypeModel batchType = batchTypeOptional.get();
        String batchTypeName = batchType.getBatchType();


        if ("custom".equalsIgnoreCase(batchTypeName)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Work From Home is not allowed for custom batches.");
        }

        if ("regular".equalsIgnoreCase(batchTypeName)) {

            boolean exists = wfhRepo.existsByUserIdAndFromDateLessThanEqualAndToDateGreaterThanEqual(
                    userId, leaveRequestDto.getToDate(), leaveRequestDto.getFromDate()
            );
            if (exists) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User already has a work-from-home request for the selected date(s).");
            }

            Wfh wfh = new Wfh();
            wfh.setFromDate(leaveRequestDto.getFromDate());
            wfh.setToDate(leaveRequestDto.getToDate());
            wfh.setReason(leaveRequestDto.getReason());
            wfh.setUserId(userId);
            wfh.setLeaveType(leaveRequestDto.getLeaveType());
            wfh.setNumberOfDays(Period.between(leaveRequestDto.getFromDate(), leaveRequestDto.getToDate()).getDays() + 1);
            wfh.setStatus(WfhStatus.PENDING);
            wfh.setName(usersModel.getName());
            wfh.setBatchId(usersModel.getBatchId());
            wfhRepo.save(wfh);

            return ResponseEntity.status(HttpStatus.CREATED).body(wfh);
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid batch type.");
    }


    public ResponseEntity<?> deleteWorkFromHomeRequest(Long requestId) {
        Optional<Wfh> wfhOptional = wfhRepo.findById(requestId);
        if (wfhOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Work from home request not found.");
        }
        wfhRepo.deleteById(requestId);
        return ResponseEntity.status(HttpStatus.OK).body("Work from home request deleted successfully.");
    }





}