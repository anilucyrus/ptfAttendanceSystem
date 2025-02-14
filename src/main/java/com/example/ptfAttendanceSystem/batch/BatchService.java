package com.example.ptfAttendanceSystem.batch;

import com.example.ptfAttendanceSystem.batchType.BatchTypeModel;
import com.example.ptfAttendanceSystem.batchType.BatchTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class BatchService {

    @Autowired
    private BatchRepository batchRepository;

    @Autowired
    private BatchTypeRepository batchTypeRepository;

    public BatchModel addBatch(BatchModel batch, Long batchTypeId) {
        // Validate input fields
        if (batch.getBatchName() == null || batch.getStartTime() == null || batch.getEndTime() == null) {
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
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No batches found");
        }
        return batches;
    }

    public BatchModel updateBatch(Long id, BatchModel updatedBatch, Long batchTypeId) {
        return batchRepository.findById(id)
                .map(existingBatch -> {
                    existingBatch.setBatchName(updatedBatch.getBatchName());
                    existingBatch.setStartTime(updatedBatch.getStartTime());
                    existingBatch.setEndTime(updatedBatch.getEndTime());

                    BatchTypeModel batchType = batchTypeRepository.findById(batchTypeId)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Batch type not found"));
                    existingBatch.setBatchType(batchType);

                    return batchRepository.save(existingBatch);
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Batch not found"));
    }

}