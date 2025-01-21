package com.example.ptfAttendanceSystem.late;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LateRequestRepository  extends JpaRepository<LateRequestModel, Long> {

    List<LateRequestModel> findByUserId(Long userId);

    List<LateRequestModel>findByStatus(LateRequestStatus status);

    List<LateRequestModel>Date(LocalDate localDate);

    Optional<LateRequestModel> findByUserIdAndDate(Long userId, LocalDate date);

}
