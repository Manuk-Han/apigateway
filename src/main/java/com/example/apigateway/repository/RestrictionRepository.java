package com.example.apigateway.repository;

import com.example.apigateway.entity.Problem;
import com.example.apigateway.entity.Restriction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RestrictionRepository extends JpaRepository<Restriction, Long> {
    void deleteRestrictionsByProblem(Problem problem);
}
