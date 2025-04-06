package com.example.apigateway.controller;

import com.example.apigateway.form.problem.ProblemCreateForm;
import com.example.apigateway.service.ProblemService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Profile({"8081"})
@RequiredArgsConstructor
@RestController
@RequestMapping("/problem")
public class ProblemController {

    private final ProblemService problemService;

    @PostMapping("/create/{courseId}")
    public ResponseEntity<Long> createProblem(@RequestHeader("X-USER-ID") Long userId, @PathVariable Long courseId, ProblemCreateForm problemCreateForm, MultipartFile file) {
        return null;
    }

    @PostMapping("/update/{problemId}")
    public ResponseEntity<Long> updateProblem(@RequestHeader("X-USER-ID") Long userId, @PathVariable Long problemId, ProblemCreateForm problemUpdateForm, MultipartFile file) {
        return null;
    }

}