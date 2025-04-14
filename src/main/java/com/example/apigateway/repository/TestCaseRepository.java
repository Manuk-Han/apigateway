package com.example.apigateway.repository;

import com.example.apigateway.entity.Problem;
import com.example.apigateway.entity.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestCaseRepository extends JpaRepository<TestCase, Long> {
    void deleteTestCasesByProblem(Problem problem);
}
