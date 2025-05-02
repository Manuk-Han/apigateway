package com.example.apigateway.controller;

import com.example.apigateway.dto.problem.ProblemDetailDto;
import com.example.apigateway.dto.problem.ProblemDto;
import com.example.apigateway.form.problem.ProblemCreateForm;
import com.example.apigateway.form.problem.ProblemUpdateForm;
import com.example.apigateway.service.ProblemService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Profile({"course"})
@RequiredArgsConstructor
@RestController
@RequestMapping("/problem")
public class ProblemController {

    private final ProblemService problemService;

    @GetMapping("/list/{courseUUId}")
    public ResponseEntity<List<ProblemDto>> getProblemList(@RequestHeader("X-USER-ID") Long userId, @PathVariable String courseUUId) {
        return ResponseEntity.ok()
                .body(problemService.getProblemList(userId, courseUUId));
    }

    @GetMapping("/detail/{courseUUId}")
    public ResponseEntity<ProblemDetailDto> getProblemDetail(@RequestHeader("X-USER-ID") Long userId, @PathVariable String courseUUId, Long problemId) {
        return ResponseEntity.ok()
                .body(problemService.getProblemDetail(userId, courseUUId, problemId));
    }

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

    @GetMapping("/delete/{courseUUId}")
    public ResponseEntity<?> deleteProblem(@RequestHeader("X-USER-ID") Long userId, @PathVariable String courseUUId, Long problemId) {
        problemService.deleteProblem(userId, courseUUId, problemId);

        return ResponseEntity.ok().body("Problem deleted successfully");
    }
}