package com.example.ptfAttendanceSystem.batch;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/batch")
@CrossOrigin
public class BatchController {

    @Autowired
    private BatchService batchService;

    @PostMapping(path = "/addBatch")
    public ResponseEntity<?> addBatch(@RequestBody BatchData batchData) {
        return batchService.addBatch(batchData);
    }



    @GetMapping(path = "/allBatch")
    public ResponseEntity<List<BatchData>> getAllBatch() {
        return batchService.getAllBatch();
    }

    @PutMapping(path = "/editBatch/{id}")
    public ResponseEntity<?> updateBatch(@PathVariable Integer id, @RequestBody BatchData batchData) {
        return batchService.updateBatch(id, batchData);
    }

    @DeleteMapping(path = "/deleteBatch/{id}")
    public ResponseEntity<?> deleteBatch(@PathVariable Long id) {
        try {
            boolean isDeleted = batchService.deleteBatch(id);
            if (isDeleted) {
                return new ResponseEntity<>("Batch deleted successfully", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Batch Not Found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
