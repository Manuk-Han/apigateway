package com.example.apigateway.controller;

import com.example.apigateway.dto.course.CourseDto;
import com.example.apigateway.form.result.ReceiveResultForm;
import com.example.apigateway.form.submit.SubmitForm;
import com.example.apigateway.service.SubmitService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Profile({"submit"})
@RequiredArgsConstructor
@RestController
@RequestMapping("/submit")
public class SubmitController {
    private final SubmitService submitService;

    @PostMapping("/{courseUUId}")
    public ResponseEntity<Long> submitProblem(@RequestHeader("X-USER-ID") Long userId, @PathVariable String courseUUId, SubmitForm submitForm) throws IOException {
        return ResponseEntity.ok()
                .body(submitService.submitProblem(userId, courseUUId, submitForm));
    }

    @GetMapping("/result/{submitId}")
    public ResponseEntity<String> getResult(ReceiveResultForm receiveResultForm) {
        submitService.receiveSubmitResult(receiveResultForm);

        return ResponseEntity.ok()
                .body("Result received successfully");
    }
}