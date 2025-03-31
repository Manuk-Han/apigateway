package com.example.apigateway.service;

import com.example.apigateway.common.exception.CustomException;
import com.example.apigateway.common.exception.CustomResponseException;
import com.example.apigateway.common.file.ExcelUtil;
import com.example.apigateway.entity.Course;
import com.example.apigateway.entity.CourseStudent;
import com.example.apigateway.entity.User;
import com.example.apigateway.form.course.AddStudentForm;
import com.example.apigateway.form.course.CourseCreateForm;
import com.example.apigateway.form.course.CourseUpdateForm;
import com.example.apigateway.repository.CourseRepository;
import com.example.apigateway.repository.CourseStudentRepository;
import com.example.apigateway.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Profile("8081")
@Service
@RequiredArgsConstructor
public class CourseService {
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final CourseStudentRepository courseStudentRepository;
    private final PasswordEncoder passwordEncoder;
    private final ExcelUtil excelUtil;

    public String createCourse(Long userId, CourseCreateForm courseCreateForm) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(CustomResponseException.NOT_FOUND_ACCOUNT));

        Course course = Course.builder()
                .courseName(courseCreateForm.getCourseName())
                .courseUUid(UUID.randomUUID().toString())
                .courseStart(courseCreateForm.getCourseStart())
                .courseEnd(courseCreateForm.getCourseEnd())
                .owner(user)
                .build();

        courseRepository.save(course);

        return course.getCourseUUid();
    }

    public String updateCourse(Long userId, CourseUpdateForm courseUpdateForm) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(CustomResponseException.NOT_FOUND_ACCOUNT));

        Course course = courseRepository.findById(courseUpdateForm.getCourseId())
                .orElseThrow(() -> new CustomException(CustomResponseException.NOT_FOUND_COURSE));

        if (!course.getOwner().equals(user))
            throw new CustomException(CustomResponseException.FORBIDDEN);

        course.updateCourse(courseUpdateForm);
        courseRepository.save(course);

        return course.getCourseUUid();
    }

    public Long addStudent(Long userId, String courseUUid, AddStudentForm addStudentForm) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(CustomResponseException.NOT_FOUND_ACCOUNT));

        Course course = courseRepository.findCourseByCourseUUid(courseUUid)
                .orElseThrow(() -> new CustomException(CustomResponseException.NOT_FOUND_COURSE));

        if (!course.getOwner().equals(user))
            throw new CustomException(CustomResponseException.FORBIDDEN);

        User student;

        if (userRepository.existsByAccountId(addStudentForm.getStudentId())) {
            student = userRepository.findByAccountId(addStudentForm.getStudentId())
                    .orElseThrow(() -> new CustomException(CustomResponseException.NOT_FOUND_ACCOUNT));
        } else {
            student = User.builder()
                    .name(addStudentForm.getStudentName())
                    .accountId(addStudentForm.getStudentId())
                    .password(passwordEncoder.encode(addStudentForm.getPhone().substring(addStudentForm.getPhone().length() - 4)))
                    .email(addStudentForm.getEmail())
                    .withdraw(false)
                    .build();

            userRepository.save(student);
        }

        courseStudentRepository.save(
                CourseStudent.builder()
                        .course(course)
                        .user(student)
                        .build()
        );

        return course.getCourseId();
    }

    public Long addStudents(Long userId, String courseUUid, MultipartFile file) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(CustomResponseException.NOT_FOUND_ACCOUNT));

        Course course = courseRepository.findCourseByCourseUUid(courseUUid)
                .orElseThrow(() -> new CustomException(CustomResponseException.NOT_FOUND_COURSE));

        if (!course.getOwner().equals(user))
            throw new CustomException(CustomResponseException.FORBIDDEN);

        excelUtil.addStudentByExcel(course, file);

        return course.getCourseId();
    }
}
