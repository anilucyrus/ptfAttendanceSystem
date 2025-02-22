package com.example.ptfAttendanceSystem.late;

import com.example.ptfAttendanceSystem.batch.BatchModel;
import com.example.ptfAttendanceSystem.batch.BatchRepository;
import com.example.ptfAttendanceSystem.model.UsersModel;
import com.example.ptfAttendanceSystem.model.UsersRepository;
import com.example.ptfAttendanceSystem.model.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LateRequestService {

    @Autowired
    private UsersRepository usersRepository;

    private final LateRequestRepository lateRequestRepository;
    private final UsersService usersService;
    private final BatchRepository batchRepository;

    @Autowired
    public LateRequestService(LateRequestRepository lateRequestRepository, UsersService usersService, BatchRepository batchRepository) {
        this.lateRequestRepository = lateRequestRepository;
        this.usersService = usersService;
        this.batchRepository = batchRepository;
    }

    public ResponseEntity<?> requestLate(Long userId, LateRequestDto lateRequestDto) {
        Optional<UsersModel> userOptional = usersService.getUserById(userId);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }

        UsersModel user = userOptional.get();
        LocalDate requestedDate = lateRequestDto.getDate();
        LocalDate currentDate = LocalDate.now();
        LocalTime currentTime = LocalTime.now();

        if (requestedDate.isBefore(currentDate)) {
            return ResponseEntity.badRequest().body("Late request cannot be made for a past date.");
        }

        if (lateRequestRepository.findByUserIdAndDate(userId, requestedDate).isPresent()) {
            return ResponseEntity.badRequest().body("You have already submitted a late request for this date.");
        }

        Optional<BatchModel> batchOptional = batchRepository.findById(user.getBatchId());
        if (batchOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("Batch not found.");
        }

        BatchModel batch = batchOptional.get();
        if (currentTime.isAfter(batch.getStartTime().plusMinutes(30))) {
            return ResponseEntity.badRequest().body("Late request not allowed after " + batch.getStartTime().plusMinutes(30));
        }

        LateRequestModel lateRequest = new LateRequestModel();
        lateRequest.setUserId(user.getUserId());
        lateRequest.setReason(lateRequestDto.getReason());
        lateRequest.setDate(requestedDate);
        lateRequest.setStatus(LateRequestStatus.PENDING);
        lateRequest.setBatchId(user.getBatchId());

        lateRequestRepository.save(lateRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(lateRequest);
    }

    public ResponseEntity<?> getAllLateRequestsForUser(Long userId) {
        List<LateRequestModel> lateRequests = lateRequestRepository.findByUserId(userId);

        Optional<UsersModel> usersModelOptional = usersRepository.findById(userId);
        if (usersModelOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid User ID.");
        }
        if (lateRequests.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No late requests found for the user.");
        }
        List<Map<String, Object>> responseList = lateRequests.stream().map(lateRequest -> {
            Map<String, Object> response = new HashMap<>();
            response.put("id", lateRequest.getId());
            response.put("userId", lateRequest.getUserId());
            response.put("reason", lateRequest.getReason());
            response.put("date", lateRequest.getDate());
            response.put("status", lateRequest.getStatus());

            // Fetch user details
            Optional<UsersModel> userOptional = usersRepository.findById(lateRequest.getUserId());
            if (userOptional.isPresent()) {
                response.put("userName", userOptional.get().getName()); // Assuming getName() returns the user's name
            } else {
                response.put("userName", "Unknown User");
            }

            // Fetch batch details
            Optional<BatchModel> batchOptional = batchRepository.findById(lateRequest.getBatchId());
            if (batchOptional.isPresent()) {
                response.put("batchId", batchOptional.get().getId());
                response.put("batchName", batchOptional.get().getBatchName());
            } else {
                response.put("batchId", lateRequest.getBatchId());
                response.put("batchName", "Unknown Batch");
            }

            return response;
        }).collect(Collectors.toList());

        return new ResponseEntity<>(responseList, HttpStatus.OK);
    }

    public ResponseEntity<?> deleteLateRequest(Long id) {
        if (!lateRequestRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Late request not found.");
        }
        lateRequestRepository.deleteById(id);
        return ResponseEntity.ok("Late request deleted successfully.");
    }

//    public ResponseEntity<?> updateLateRequest(Long requestId, LateRequestDto lateRequestDto) {
//        Optional<LateRequestModel> lateRequestOptional = lateRequestRepository.findById(requestId);
//        if (lateRequestOptional.isEmpty()) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Late request not found.");
//        }
//
//        LateRequestModel lateRequest = lateRequestOptional.get();
//        lateRequest.setReason(lateRequestDto.getReason());
//        lateRequest.setDate(lateRequestDto.getDate());
//        lateRequest.setStatus(LateRequestStatus.PENDING);
//
//        lateRequestRepository.save(lateRequest);
//        return ResponseEntity.ok(lateRequest);
//    }
}
