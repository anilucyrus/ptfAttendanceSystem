package com.example.ptfAttendanceSystem.admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<AdminModel,Integer> {

    Optional<AdminModel> findByEmail(String dto);
    Optional<AdminModel> findByEmailAndPassword(String email, String password);


    Optional<AdminModel> findById(Long id);
}