package com.example.apigateway.repository;

import com.example.apigateway.entity.Course;
import com.example.apigateway.entity.ProblemBank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProblemBankRepository extends JpaRepository<ProblemBank, Long> {
    boolean existsByCourse(Course course);

    ProblemBank findByCourse(Course course);
}
