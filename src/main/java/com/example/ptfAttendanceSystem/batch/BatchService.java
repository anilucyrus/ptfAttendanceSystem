package com.example.ptfAttendanceSystem.batch;

import com.example.ptfAttendanceSystem.batchType.BatchTypeModel;
import com.example.ptfAttendanceSystem.batchType.BatchTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class BatchService {

    @Autowired
    private BatchRepository batchRepository;

    @Autowired
    private BatchTypeRepository batchTypeRepository;

    public BatchModel addBatch(BatchModel batch, Long batchTypeId) {
        // Validate input fields
        if (batch.getBatchName() == null || batch.getStartTime() == null || batch.getEndTime() == null || batch.getBatchLatitude() ==null || batch.getBatchLongitude() ==null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "All fields are required.");
        }

        // Check if batch with the same name already exists
        if (batchRepository.findByBatchName(batch.getBatchName()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Batch with this name already exists.");
        }

        // Fetch batch type
        BatchTypeModel batchType = batchTypeRepository.findById(batchTypeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Batch type not found"));

        batch.setBatchType(batchType);
        return batchRepository.save(batch);
    }

    public void deleteBatch(Long id) {
        if (!batchRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Batch not found");
        }
        batchRepository.deleteById(id);
    }

    public List<BatchModel> getAllBatches() {
        List<BatchModel> batches = batchRepository.findAll();
        if (batches.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NO_CONTENT, "No batches found");
        }
        return batches;
    }

//
//
//    public BatchModel updateBatch(Long id, BatchModel updatedBatch, Long batchTypeId) {
//        return batchRepository.findById(id)
//                .map(existingBatch -> {
//                    if (updatedBatch.getBatchName() != null) {
//                        existingBatch.setBatchName(updatedBatch.getBatchName());
//                    }
//                    if (updatedBatch.getStartTime() != null) {
//                        existingBatch.setStartTime(updatedBatch.getStartTime());
//                    }
//                    if (updatedBatch.getEndTime() != null) {
//                        existingBatch.setEndTime(updatedBatch.getEndTime());
//                    }
//                    if (batchTypeId != null) {
//                        BatchTypeModel batchType = batchTypeRepository.findById(batchTypeId)
//                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Batch type not found"));
//                        existingBatch.setBatchType(batchType);
//                    }
//
//                    return batchRepository.save(existingBatch);
//                })
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Batch not found"));
//    }



    public BatchModel updateBatch(Long batchId, BatchModel updatedBatch) {
        BatchModel existingBatch = batchRepository.findById(batchId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Batch not found"));

        existingBatch.setBatchName(updatedBatch.getBatchName());
        existingBatch.setStartTime(updatedBatch.getStartTime());
        existingBatch.setEndTime(updatedBatch.getEndTime());
        existingBatch.setBatchLatitude(updatedBatch.getBatchLatitude());
        existingBatch.setBatchLongitude(updatedBatch.getBatchLongitude());

        if (updatedBatch.getBatchType() != null && updatedBatch.getBatchType().getId() != null) {
            BatchTypeModel batchType = batchTypeRepository.findById(updatedBatch.getBatchType().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Batch type not found"));
            existingBatch.setBatchType(batchType);
        }

        return batchRepository.save(existingBatch);
    }

    public String getBatchNameById(Long batchId) {
        return batchRepository.findById(batchId)
                .map(BatchModel::getBatchName)
                .orElse("Unknown Batch");
    }


    public Optional<BatchModel> getBatchByName(String batchName) {
        return batchRepository.findByBatchName(batchName);
    }

    public Optional<BatchModel> getBatchById(Long batchId) {
        return batchRepository.findById(batchId);
    }

}