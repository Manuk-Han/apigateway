package com.example.apigateway.repository;

import com.example.apigateway.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByAccountId(String loginId);

    boolean existsByAccountId(String accountId);
}
