package com.example.apigateway.service;

import com.example.apigateway.common.exception.CustomException;
import com.example.apigateway.common.exception.CustomResponseException;
import com.example.apigateway.common.file.ExcelUtil;
import com.example.apigateway.common.type.Status;
import com.example.apigateway.dto.course.CourseDto;
import com.example.apigateway.dto.course.CourseGradeDto;
import com.example.apigateway.entity.*;
import com.example.apigateway.form.course.AddStudentForm;
import com.example.apigateway.form.course.CourseCreateForm;
import com.example.apigateway.form.course.CourseUpdateForm;
import com.example.apigateway.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Profile("8081")
@Service
@RequiredArgsConstructor
public class CourseService {
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final CourseStudentRepository courseStudentRepository;
    private final ProblemBankRepository problemBankRepository;
    private final SubmitRepository submitRepository;
    private final PasswordEncoder passwordEncoder;
    private final ExcelUtil excelUtil;

    public List<CourseDto> getOwnCourseList(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(CustomResponseException.NOT_FOUND_ACCOUNT));

        List<Course> courseList = courseRepository.findAllByOwner(user);

        return courseList.stream()
                .map(course -> CourseDto.builder()
                        .courseName(course.getCourseName())
                        .courseUUid(course.getCourseUUid())
                        .build())
                .collect(Collectors.toList());
    }

    public List<CourseDto> getCourseList(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(CustomResponseException.NOT_FOUND_ACCOUNT));

        return user.getCourseStudentList().stream()
                .map(courseStudent -> CourseDto.builder()
                        .courseName(courseStudent.getCourse().getCourseName())
                        .courseUUid(courseStudent.getCourse().getCourseUUid())
                        .build())
                .collect(Collectors.toList());
    }

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
        problemBankRepository.save(
                ProblemBank.builder()
                        .course(course)
                        .build()
        );

        return course.getCourseUUid();
    }

    public String updateCourse(Long userId, CourseUpdateForm courseUpdateForm) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(CustomResponseException.NOT_FOUND_ACCOUNT));

        Course course = courseRepository.findById(courseUpdateForm.getCourseId())
                .orElseThrow(() -> new CustomException(CustomResponseException.NOT_FOUND_COURSE));

        validateCourseOwner(user, course);

        course.updateCourse(courseUpdateForm);
        courseRepository.save(course);

        return course.getCourseUUid();
    }

    public String deleteCourse(Long userId, String courseUUid) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new CustomException(CustomResponseException.NOT_FOUND_ACCOUNT));

            Course course = courseRepository.findCourseByCourseUUid(courseUUid)
                    .orElseThrow(() -> new CustomException(CustomResponseException.NOT_FOUND_COURSE));

            validateCourseOwner(user, course);

            courseRepository.delete(course);
        } catch (Exception e) {
            throw new CustomException(CustomResponseException.SERVER_ERROR);
        }

        return courseUUid;
    }

    public Long addStudent(Long userId, String courseUUid, AddStudentForm addStudentForm) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(CustomResponseException.NOT_FOUND_ACCOUNT));

        Course course = courseRepository.findCourseByCourseUUid(courseUUid)
                .orElseThrow(() -> new CustomException(CustomResponseException.NOT_FOUND_COURSE));

        validateCourseOwner(user, course);

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

    @Value("${file.sample.path}")
    private String sampleExcelFilePath;
    private final ProblemRepository problemRepository;

    public byte[] getSampleExcel() throws IOException {
        File file = new File(sampleExcelFilePath);

        if (!file.exists()) {
            throw new CustomException(CustomResponseException.FILE_NOT_FOUND);
        }

        return Files.readAllBytes(file.toPath());
    }

    public Long addStudentsByFile(Long userId, String courseUUid, MultipartFile file) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(CustomResponseException.NOT_FOUND_ACCOUNT));

        Course course = courseRepository.findCourseByCourseUUid(courseUUid)
                .orElseThrow(() -> new CustomException(CustomResponseException.NOT_FOUND_COURSE));

        validateCourseOwner(user, course);

        excelUtil.addStudentByExcel(course, file);

        return course.getCourseId();
    }

    public Long kickStudent(Long userId, String courseUUid, String studentId) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(CustomResponseException.NOT_FOUND_ACCOUNT));

        Course course = courseRepository.findCourseByCourseUUid(courseUUid)
                .orElseThrow(() -> new CustomException(CustomResponseException.NOT_FOUND_COURSE));

        validateCourseOwner(user, course);

        User student = userRepository.findByAccountId(studentId)
                .orElseThrow(() -> new CustomException(CustomResponseException.NOT_FOUND_ACCOUNT));

        CourseStudent courseStudent = courseStudentRepository.findByCourseAndUser(course, student)
                .orElseThrow(() -> new CustomException(CustomResponseException.NOT_COURSE_STUDENT));

        courseStudentRepository.delete(courseStudent);

        return course.getCourseId();
    }

    public List<CourseGradeDto> getCourseGrade(Long userId, String courseUUid) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(CustomResponseException.NOT_FOUND_ACCOUNT));

        Course course = courseRepository.findCourseByCourseUUid(courseUUid)
                .orElseThrow(() -> new CustomException(CustomResponseException.NOT_FOUND_COURSE));

        validateCourseOwner(user, course);

        List<CourseGradeDto> courseGradeList = new ArrayList<>();

        course.getProblemBank().getProblemList().forEach(
                problem -> course.getCourseStudentList().forEach(
                        student -> courseGradeList.add(makeCourseGradeDto(student.getUser(), problem,
                                submitRepository.findTopScoreSubmitByProblemAndStudent(problem.getProblemId(), student.getUser().getUserId())))
                )
        );

        return courseGradeList;
    }

    public List<CourseGradeDto> getCourseGradeWithProblem(Long userId, String courseUUid, Long problemId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(CustomResponseException.NOT_FOUND_ACCOUNT));

        Course course = courseRepository.findCourseByCourseUUid(courseUUid)
                .orElseThrow(() -> new CustomException(CustomResponseException.NOT_FOUND_COURSE));

        validateCourseOwner(user, course);

        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new CustomException(CustomResponseException.NOT_FOUND_PROBLEM));

        List<CourseGradeDto> courseGradeList = new ArrayList<>();

        course.getCourseStudentList().forEach(
                student -> courseGradeList.add(makeCourseGradeDto(student.getUser(), problem,
                        submitRepository.findTopScoreSubmitByProblemAndStudent(problem.getProblemId(), student.getUser().getUserId())))
        );

        return courseGradeList;
    }

    private CourseGradeDto makeCourseGradeDto(User student, Problem problem, Submit submit) {
        return submit == null ?
                CourseGradeDto.builder()
                        .accountId(student.getAccountId())
                        .studentName(student.getName())
                        .problemId(problem.getProblemId())
                        .problemTitle(problem.getProblemTitle())
                        .status(Status.NOT_SUBMITTED)
                        .score(0)
                        .submitId(null)
                        .build()
                :
                CourseGradeDto.builder()
                        .accountId(student.getAccountId())
                        .studentName(student.getName())
                        .problemId(problem.getProblemId())
                        .problemTitle(problem.getProblemTitle())
                        .score(submit.getResult().getScore())
                        .status(submit.getResult().getStatus())
                        .submitId(submit.getSubmitId())
                        .build();
    }

    private void validateCourseOwner(User user, Course course) {
        if (!course.getOwner().equals(user)) {
            throw new CustomException(CustomResponseException.FORBIDDEN);
        }
    }

}
