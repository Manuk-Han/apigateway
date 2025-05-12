package com.example.apigateway.repository;

import com.example.apigateway.common.type.Status;
import com.example.apigateway.entity.Result;
import com.example.apigateway.entity.Submit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResultRepository extends JpaRepository<Result, Long> {
    Optional<Result> findResultBySubmit(Submit submit);

    @Query(value = """
    SELECT MIN(CASE r.status
                 WHEN 'PASS' THEN 1
                 WHEN 'REJECTED' THEN 2
                 WHEN 'CORRECT' THEN 3
                 WHEN 'WRONG' THEN 4
                 WHEN 'ERROR' THEN 5
                 WHEN 'NOT_SUBMITTED' THEN 6
                 ELSE 6
               END)
    FROM problem p
    JOIN problem_bank pb ON p.problem_bank_id = pb.problem_bank_id
    JOIN course c ON pb.course_id = c.course_id
    JOIN submit s ON s.problem_id = p.problem_id
    JOIN result r ON r.submit_id = s.submit_id
    WHERE p.problem_id = :problemId
      AND c.courseuuid = :courseUUId
      AND s.student_id = :userId
    """, nativeQuery = true)
    Integer getBestStatusPriority(@Param("userId") Long userId,
                                  @Param("courseUUId") String courseUUId,
                                  @Param("problemId") Long problemId);

}
