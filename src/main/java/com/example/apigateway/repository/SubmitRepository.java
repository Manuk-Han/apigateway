package com.example.apigateway.repository;

import com.example.apigateway.entity.Submit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubmitRepository extends JpaRepository<Submit, Long> {
}
