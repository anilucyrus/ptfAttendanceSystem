package com.example.ptfAttendanceSystem.batch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BatchService {
    @Autowired
    private BatchRepository batchRepository;

    public BatchModel addBatch(BatchModel batch) {
        return batchRepository.save(batch);
    }

    public void deleteBatch(Long id) {
        batchRepository.deleteById(id);
    }

    public List<BatchModel> getAllBatches() {
        return batchRepository.findAll();
    }
}