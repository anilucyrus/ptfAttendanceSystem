package com.example.ptfAttendanceSystem.batch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BatchService {


    @Autowired
    private BatchRepo batchRepo;

    public ResponseEntity<?> addBatch(BatchData batchData) {
        Optional<BatchData> existingBatch = batchRepo.findByBatch(batchData.getBatch());
        if (existingBatch.isPresent()) {
            return new ResponseEntity<>("Batch already exists", HttpStatus.CONFLICT);
        }
        batchRepo.save(batchData);
        return new ResponseEntity<>("Batch added successfully", HttpStatus.CREATED);
    }

    public ResponseEntity<?> deleteBatch(String batch) {
        Optional<BatchData> existingBatch = batchRepo.findByBatch(batch);
        if (existingBatch.isEmpty()) {
            return new ResponseEntity<>("Batch not found", HttpStatus.NOT_FOUND);
        }
        batchRepo.deleteByBatch(batch);
        return new ResponseEntity<>("Batch deleted successfully", HttpStatus.OK);
    }
}

