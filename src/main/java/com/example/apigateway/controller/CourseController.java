package com.example.apigateway.controller;

import com.example.apigateway.dto.course.CourseDto;
import com.example.apigateway.dto.course.CourseGradeDto;
import com.example.apigateway.dto.course.CourseGradeInfoDto;
import com.example.apigateway.dto.course.StudentInfoDTO;
import com.example.apigateway.form.course.AddStudentByFileForm;
import com.example.apigateway.form.course.AddStudentForm;
import com.example.apigateway.form.course.CourseCreateForm;
import com.example.apigateway.form.course.CourseUpdateForm;
import com.example.apigateway.service.CourseService;
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
@RequestMapping("/course")
public class CourseController {
    private final CourseService courseService;

    @GetMapping("/list")
    public ResponseEntity<List<CourseDto>> courseList(@RequestHeader("X-USER-ID") Long userId) {
        return ResponseEntity.ok()
                .body(courseService.getCourseList(userId));
    }

    @GetMapping("/own-list")
    public ResponseEntity<List<CourseDto>> ownCourseList(@RequestHeader("X-USER-ID") Long userId) {
        return ResponseEntity.ok()
                .body(courseService.getOwnCourseList(userId));
    }

    @PostMapping("/create")
    public ResponseEntity<String> create(@RequestHeader("X-USER-ID") Long userId, CourseCreateForm courseCreateForm) {
        return ResponseEntity.ok()
                .body(courseService.createCourse(userId, courseCreateForm));
    }

    @PostMapping("/update/{courseUUId}")
    public ResponseEntity<String> update(@RequestHeader("X-USER-ID") Long userId, @PathVariable String courseUUId, CourseUpdateForm courseUpdateForm) {
        return ResponseEntity.ok()
                .body(courseService.updateCourse(userId, courseUUId, courseUpdateForm));
    }

    @GetMapping("/delete")
    public ResponseEntity<String> delete(@RequestHeader("X-USER-ID") Long userId, String courseUUid) {
        return ResponseEntity.ok()
                .body(courseService.deleteCourse(userId, courseUUid));
    }

    @GetMapping("/student/info/list/{courseUUId}")
    public ResponseEntity<List<StudentInfoDTO>> getStudentInfoList(@RequestHeader("X-USER-ID") Long userId, @PathVariable String courseUUId) throws IOException {
        return ResponseEntity.ok(courseService.getStudentInfoList(userId, courseUUId));
    }

    @GetMapping("/student/info/{courseUUId}")
    public ResponseEntity<StudentInfoDTO> getStudentInfo(@RequestHeader("X-USER-ID") Long userId, @PathVariable String courseUUId, String studentId) throws IOException {
        return ResponseEntity.ok(courseService.getStudentInfo(userId, studentId, courseUUId));
    }

    @PostMapping("/invite/{courseUUId}")
    public ResponseEntity<Long> inviteOne(@RequestHeader("X-USER-ID") Long userId, @PathVariable String courseUUId, AddStudentForm addStudentForm) throws IOException {
        return ResponseEntity.ok(courseService.addStudent(userId, courseUUId, addStudentForm));
    }

    @GetMapping("/invite/sample/download")
    public ResponseEntity<?> downloadSample(@RequestHeader("X-USER-ID") Long userId) throws IOException {
        byte[] fileContent = courseService.getSampleExcel();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=code_quest_invite_sample.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .body(fileContent);
    }

    @PostMapping("/invite-file/{courseUUId}")
    public Mono<ResponseEntity<Long>> inviteAll(
            @RequestHeader("X-USER-ID") Long userId,
            @PathVariable String courseUUId,
            @RequestPart("file") FilePart file) {

        return courseService.addStudentsByFile(userId, courseUUId, file)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/kick/{courseUUId}")
    public ResponseEntity<Long> kick(@RequestHeader("X-USER-ID") Long userId, @PathVariable String courseUUId, String studentId) throws IOException {
        return ResponseEntity.ok(courseService.kickStudent(userId, courseUUId, studentId));
    }

    @GetMapping("/all/grade/{courseUUId}")
    public ResponseEntity<List<CourseGradeInfoDto>> getCourseGrade(@RequestHeader("X-USER-ID") Long userId, @PathVariable String courseUUId) throws IOException {
        return ResponseEntity.ok(courseService.getCourseGrade(userId, courseUUId));
    }

    @GetMapping("/problem/grade/{courseUUId}")
    public ResponseEntity<CourseGradeInfoDto> getCourseGradeWithProblem(@RequestHeader("X-USER-ID") Long userId, @PathVariable String courseUUId, Long problemId) throws IOException {
        return ResponseEntity.ok(courseService.getCourseGradeWithProblem(userId, courseUUId, problemId));
    }
}