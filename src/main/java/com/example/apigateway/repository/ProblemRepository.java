package com.example.apigateway.repository;

import com.example.apigateway.entity.Problem;
import com.example.apigateway.entity.ProblemBank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProblemRepository extends JpaRepository<Problem, Long> {
    void deleteProblemByProblemIdAndProblemBank(Long problemId, ProblemBank problemBank);

    Optional<Problem> findByProblemIdAndProblemBank(Long problemId, ProblemBank problemBank);
}
