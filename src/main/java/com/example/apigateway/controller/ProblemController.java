package com.example.apigateway.controller;

import com.example.apigateway.dto.problem.ProblemDetailDto;
import com.example.apigateway.dto.problem.ProblemDto;
import com.example.apigateway.form.problem.ProblemCreateForm;
import com.example.apigateway.form.problem.ProblemUpdateForm;
import com.example.apigateway.service.ProblemService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

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

    @GetMapping("/testcase/sample/download")
    public ResponseEntity<?> downloadSample(@RequestHeader("X-USER-ID") Long userId) throws IOException {
        byte[] fileContent = problemService.getSampleExcel();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=code_quest_invite_sample.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .body(fileContent);
    }

    @PostMapping("/create/{courseUUId}")
    public Mono<ResponseEntity<Long>> createProblem(@RequestHeader("X-USER-ID") Long userId, @PathVariable String courseUUId, ProblemCreateForm problemCreateForm, @RequestPart("file") FilePart file) throws IOException {
        return problemService.createProblem(userId, courseUUId, problemCreateForm, file)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/update/{courseUUId}")
    public Mono<ResponseEntity<Long>> updateProblem(@RequestHeader("X-USER-ID") Long userId, @PathVariable String courseUUId, ProblemUpdateForm problemUpdateForm, @RequestPart(name = "file", required = false) FilePart file) throws IOException {
        return problemService.updateProblem(userId, courseUUId, problemUpdateForm, file)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/delete/{courseUUId}")
    public ResponseEntity<?> deleteProblem(@RequestHeader("X-USER-ID") Long userId, @PathVariable String courseUUId, Long problemId) {
        problemService.deleteProblem(userId, courseUUId, problemId);

        return ResponseEntity.ok().body("Problem deleted successfully");
    }
}