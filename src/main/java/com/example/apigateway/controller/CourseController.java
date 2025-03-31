package com.example.apigateway.controller;

import com.example.apigateway.form.course.AddStudentForm;
import com.example.apigateway.form.course.CourseCreateForm;
import com.example.apigateway.form.course.CourseUpdateForm;
import com.example.apigateway.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Profile({"8081"})
@RequiredArgsConstructor
@RestController
@RequestMapping("/course")
public class CourseController {
    private final CourseService courseService;

    @PostMapping("/create")
    public ResponseEntity<String> create(@RequestHeader("X-USER-ID") Long userId, CourseCreateForm courseCreateForm) {
        return ResponseEntity.ok()
                .body(courseService.createCourse(userId, courseCreateForm));
    }

    @PostMapping("/update")
    public ResponseEntity<String> update(@RequestHeader("X-USER-ID") Long userId, CourseUpdateForm courseUpdateForm) {
        return ResponseEntity.ok()
                .body(courseService.updateCourse(userId, courseUpdateForm));
    }

    @PostMapping("/invite/{courseId}")
    public ResponseEntity<Long> inviteOne(@RequestHeader("X-USER-ID") Long userId, @PathVariable String courseId, AddStudentForm addStudentForm) throws IOException {
        return ResponseEntity.ok(courseService.addStudent(userId, courseId, addStudentForm));
    }

    @PostMapping("/invite-file/{courseId}")
    public ResponseEntity<Long> inviteAll(@RequestHeader("X-USER-ID") Long userId, @PathVariable String courseId, MultipartFile file) throws IOException {
        return ResponseEntity.ok(courseService.addStudents(userId, courseId, file));
    }
}