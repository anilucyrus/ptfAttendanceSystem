package com.example.ptfAttendanceSystem.leaveAndWFH;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class WfhService {

    @Autowired
    private WfhRepo wfhRepo;
    @Autowired
    private LeaveWfhRepo leaveWfhRepo;

    public List<LeaveWfh> getAllWfh() {
        return leaveWfhRepo.findAll();
    }

    public LeaveWfh addWfh(LeaveWfh leaveWfh) {
        return leaveWfhRepo.save(leaveWfh);
    }

    public LeaveWfh updateWfh(Integer id, LeaveWfh leaveWfhDetails) {
        Optional<LeaveWfh> optionalWfh = leaveWfhRepo.findById(id);
        if (optionalWfh.isPresent()) {
            LeaveWfh existingWfh = optionalWfh.get();
            existingWfh.setName(leaveWfhDetails.getName());
            return leaveWfhRepo.save(existingWfh);
        } else {
            throw new RuntimeException("WFH entry not found  " );
        }
    }

    public void deleteWfh(Integer id) {
        if (leaveWfhRepo.existsById(id)) {
            leaveWfhRepo.deleteById(id);
        } else {
            throw new RuntimeException("WFH entry not found ");
        }
    }

    public List<Wfh> getWfhRequestsByStatusAndBatch(WfhStatus status, Long batchId) {
        return wfhRepo.findByStatusAndBatchId(status, batchId);
    }

}
