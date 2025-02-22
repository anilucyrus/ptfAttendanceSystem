package com.example.ptfAttendanceSystem.batchType;



import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BatchTypeService {


    @Autowired
    private BatchTypeRepository batchTypeRepository;


    public String addBatchType(BatchTypeModel batchType) {
        if (batchTypeRepository.findByBatchType(batchType.getBatchType()).isPresent()) {
            return "Batch type already exists.";
        }
        batchTypeRepository.save(batchType);
        return "Batch type added successfully.";
    }

    public void deleteBatchType(Long id) {
        if (!batchTypeRepository.existsById(id)) {
            throw new EntityNotFoundException("Batch type not found");
        }
        batchTypeRepository.deleteById(id);
    }

    public ResponseEntity<?> getAllBatchType() {
        List<BatchTypeModel> batchTypes = batchTypeRepository.findAll();
        if (batchTypes.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Batch type not found");
        }
        return ResponseEntity.ok(batchTypes);
    }


    public BatchTypeModel updateBatchType(BatchTypeModel batchType) {
        return batchTypeRepository.findById(batchType.getId())
                .map(existingBatchType -> {
                    existingBatchType.setBatchType(batchType.getBatchType());
                    return batchTypeRepository.save(existingBatchType);
                })
                .orElseThrow(() -> new RuntimeException("Batch type not found " ));
    }

}

