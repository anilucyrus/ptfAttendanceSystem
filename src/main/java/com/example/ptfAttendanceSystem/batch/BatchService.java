package com.example.ptfAttendanceSystem.batch;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BatchService {


    @Autowired
    private BatchRepo batchRepo;

    public ResponseEntity<?> addBatch(BatchData batchData) {
        batchRepo.save(batchData);
        return new ResponseEntity<>(batchData, HttpStatus.OK);
    }


    public ResponseEntity<List<BatchData>> getAllBatch() {
        List<BatchData> batchData = batchRepo.findAll();
        return new ResponseEntity<>(batchData, HttpStatus.OK);
    }



    public ResponseEntity<?> updateBatch(Integer id, BatchData batchData) {
        try {
            Optional<BatchData> existingBatch = batchRepo.findById(id);
            if (existingBatch.isPresent()) {
                BatchData updatedBatch = existingBatch.get();
                updatedBatch.setBatch(batchData.getBatch());
                updatedBatch.setStartingTime(batchData.getStartingTime());
                updatedBatch.setEndingTime(batchData.getEndingTime());
                batchRepo.save(updatedBatch);
                return new ResponseEntity<>("Batch updated successfully", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Batch not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Error updating Batch", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
//
//    public ResponseEntity<?> updateBatch(Integer id, BatchData batchData) {
//        try {
//            Optional<BatchData> existingBatch =batchRepo.findById(id);
//            if (existingBatch.isPresent()) {
//               BatchData updatedBatch = existingBatch.get();
//                updatedBatch.setBatch(batchData.getBatch());
//                batchRepo.save(updatedBatch);
//                return new ResponseEntity<>("District updated successfully", HttpStatus.OK);
//            } else {
//                return new ResponseEntity<>("District not found", HttpStatus.NOT_FOUND);
//            }
//        } catch (Exception e) {
//            return new ResponseEntity<>("Error updating District", HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }

    public boolean deleteBatch(Long id) {
        Optional<BatchData> batchData = batchRepo.findById(id);
        if (!batchData.isPresent()) {
            return false;
        }
        batchRepo.delete(batchData.get());
        return true;
    }
}
