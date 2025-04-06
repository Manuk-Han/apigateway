package com.example.apigateway.repository;

import com.example.apigateway.entity.Submit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubmitRepository extends JpaRepository<Submit, Long> {
    @Query("""
    SELECT s FROM Submit s
    JOIN s.result r
    WHERE s.problem.problemId = :problemId
    AND s.student.userId = :studentId
    AND r.score = (
        SELECT MAX(r2.score)
        FROM Submit s2
        JOIN s2.result r2
        WHERE s2.problem.problemId = :problemId
        AND s2.student.userId = :studentId
    )
    ORDER BY s.submitTime ASC
    LIMIT 1
""")
    Submit findTopScoreSubmitByProblemAndStudent(@Param("problemId") Long problemId,
                                                 @Param("studentId") Long studentId);
}
