package com.example.apigateway.repository;

import com.example.apigateway.entity.Example;
import com.example.apigateway.entity.Problem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExampleRepository extends JpaRepository<Example, Long> {
    void deleteExamplesByProblem(Problem problem);
}
