package com.example.apigateway.repository;

import com.example.apigateway.entity.Result;
import com.example.apigateway.entity.Submit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResultRepository extends JpaRepository<Result, Long> {
    Optional<Result> findResultBySubmit(Submit submit);
}
