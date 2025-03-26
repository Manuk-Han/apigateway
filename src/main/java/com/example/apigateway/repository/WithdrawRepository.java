package com.example.apigateway.repository;

import com.example.apigateway.entity.Course;
import com.example.apigateway.entity.User;
import com.example.apigateway.entity.Withdraw;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WithdrawRepository extends JpaRepository<Withdraw, Long> {
    Optional<Withdraw> findByUserAndCode(User user, String code);
}
