package com.example.ptfAttendanceSystem.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<UsersModel, Long> {
    Optional<UsersModel> findByEmail(String email);
    Optional<UsersModel> findById(Long id);
    Optional<UsersModel> findByEmailAndPassword(String email, String password);
    Optional<UsersModel> findByToken(String token);
}


