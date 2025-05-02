package com.example.apigateway.controller;

import com.example.apigateway.dto.result.ResultDetailDto;
import com.example.apigateway.dto.result.ResultDto;
import com.example.apigateway.form.result.FeedbackForm;
import com.example.apigateway.service.ResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Profile({"course"})
@RequiredArgsConstructor
@RestController
@RequestMapping("/result")
public class ResultController {
    private final ResultService resultService;

    @GetMapping("/list/{courseUUId}")
    public ResponseEntity<List<ResultDto>> getResultList(@RequestHeader("X-USER-ID") Long userId, @PathVariable String courseUUId, Long problemId) {
        return ResponseEntity.ok()
                .body(resultService.getResultList(userId, courseUUId, problemId));
    }

    @GetMapping("/manager/list/{courseUUId}")
    public ResponseEntity<List<ResultDto>> getManagerResultList(@RequestHeader("X-USER-ID") Long userId, @PathVariable String courseUUId, Long problemId) {
        return ResponseEntity.ok()
                .body(resultService.getResultListForClassOwner(userId, courseUUId, problemId));
    }

    @GetMapping("/detail/{courseUUId}")
    public ResponseEntity<ResultDetailDto> getResultDetail(@RequestHeader("X-USER-ID") Long userId, @PathVariable String courseUUId, Long resultId) {
        return ResponseEntity.ok()
                .body(resultService.getResultDetail(userId, courseUUId, resultId));
    }

    @PostMapping("/add/feedback/{courseUUId}")
    public ResponseEntity<Long> setFeedback(@RequestHeader("X-USER-ID") Long userId, @PathVariable String courseUUId, FeedbackForm feedbackForm) {
        return ResponseEntity.ok()
                .body(resultService.setFeedback(userId, courseUUId, feedbackForm));
    }
}