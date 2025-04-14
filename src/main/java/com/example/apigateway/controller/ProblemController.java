package com.example.apigateway.controller;

import com.example.apigateway.form.problem.ProblemCreateForm;
import com.example.apigateway.form.problem.ProblemUpdateForm;
import com.example.apigateway.service.ProblemService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Profile({"8081"})
@RequiredArgsConstructor
@RestController
@RequestMapping("/problem")
public class ProblemController {

    private final ProblemService problemService;

    @PostMapping("/create/{courseUUId}")
    public ResponseEntity<Long> createProblem(@RequestHeader("X-USER-ID") Long userId, @PathVariable String courseUUId, ProblemCreateForm problemCreateForm, MultipartFile file) throws IOException {
        return ResponseEntity.ok()
                .body(problemService.createProblem(userId, courseUUId, problemCreateForm, file));
    }

    @PostMapping("/update/{courseUUId}")
    public ResponseEntity<Long> updateProblem(@RequestHeader("X-USER-ID") Long userId, @PathVariable String courseUUId, ProblemUpdateForm problemUpdateForm, MultipartFile file) throws IOException {
        return ResponseEntity.ok()
                .body(problemService.updateProblem(userId, courseUUId, problemUpdateForm, file));
    }

}