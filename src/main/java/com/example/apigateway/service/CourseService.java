package com.example.apigateway.service;

import com.example.apigateway.common.exception.CustomException;
import com.example.apigateway.common.exception.CustomResponseException;
import com.example.apigateway.common.file.ExcelUtil;
import com.example.apigateway.common.type.InviteType;
import com.example.apigateway.common.type.Role;
import com.example.apigateway.common.type.Status;
import com.example.apigateway.dto.course.CourseDto;
import com.example.apigateway.dto.course.CourseGradeDto;
import com.example.apigateway.dto.course.InviteStudentDto;
import com.example.apigateway.dto.course.StudentInfoDTO;
import com.example.apigateway.entity.*;
import com.example.apigateway.form.course.AddStudentForm;
import com.example.apigateway.form.course.CourseCreateForm;
import com.example.apigateway.form.course.CourseUpdateForm;
import com.example.apigateway.repository.*;
import com.example.apigateway.service.common.ValidateUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Profile("course")
@Service
@RequiredArgsConstructor
@Transactional
public class CourseService {
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final CourseStudentRepository courseStudentRepository;
    private final UserRoleRepository userRoleRepository;
    private final ProblemBankRepository problemBankRepository;
    private final ProblemRepository problemRepository;
    private final SubmitRepository submitRepository;
    private final PasswordEncoder passwordEncoder;
    private final ExcelUtil excelUtil;
    private final ValidateUtil validateUtil;

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

    public String updateCourse(Long userId, String courseUUId, CourseUpdateForm courseUpdateForm) {
        Course course = validateUtil.validateCourseOwner(userId, courseUUId);

        course.updateCourse(courseUpdateForm);
        courseRepository.save(course);

        return course.getCourseUUid();
    }

    public String deleteCourse(Long userId, String courseUUid) {
        try {
            Course course = validateUtil.validateCourseOwner(userId, courseUUid);

            courseRepository.delete(course);
        } catch (Exception e) {
            throw new CustomException(CustomResponseException.SERVER_ERROR);
        }

        return courseUUid;
    }

    public List<StudentInfoDTO> getStudentInfoList(Long userId, String courseUUid) {
        Course course = validateUtil.validateCourseOwner(userId, courseUUid);

        List<CourseStudent> courseStudentList = courseStudentRepository.findCourseStudentsByCourse(course);

        return courseStudentList.stream()
                .map(courseStudent -> StudentInfoDTO.builder()
                        .accountId(courseStudent.getUser().getAccountId())
                        .name(courseStudent.getUser().getName())
                        .email(courseStudent.getUser().getEmail())
                        .inviteType(courseStudent.getInviteType())
                        .build())
                .collect(Collectors.toList());
    }

    public Long addStudent(Long userId, String courseUUid, AddStudentForm addStudentForm) {
        Course course = validateUtil.validateCourseOwner(userId, courseUUid);

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

            Role.getRoles(Role.USER)
                    .forEach(role -> userRoleRepository.save(
                            UserRole.builder()
                                    .user(student)
                                    .role(role)
                                    .build()
                    ));
        }

        courseStudentRepository.save(
                CourseStudent.builder()
                        .course(course)
                        .user(student)
                        .inviteType(InviteType.ONE)
                        .build()
        );

        return course.getCourseId();
    }

    @Value("${file.sampel.invite.path}")
    private String sampleExcelFilePath;

    public byte[] getSampleExcel() throws IOException {
        File file = new File(sampleExcelFilePath);

        if (!file.exists()) {
            throw new CustomException(CustomResponseException.FILE_NOT_FOUND);
        }

        return Files.readAllBytes(file.toPath());
    }

    public Mono<Long> addStudentsByFile(Long userId, String courseUUid, FilePart file) {
        Course course = validateUtil.validateCourseOwner(userId, courseUUid);

//        courseStudentRepository.deleteCourseStudentsByCourseAndInviteType(course, InviteType.FILE);

        return excelUtil.addStudents(file, course)
                .flatMap(studentDtoList -> Mono.fromRunnable(() -> saveStudentsToCourse(course, studentDtoList))
                        .subscribeOn(Schedulers.boundedElastic()))
                .then(excelUtil.saveInviteFileRecord(file, course))
                .thenReturn(course.getCourseId());
    }

    public void saveStudentsToCourse(Course course, List<InviteStudentDto> studentDtoList) {
        for (InviteStudentDto dto : studentDtoList) {
            User student;

            if (!userRepository.existsByAccountId(dto.getAccountId())) {
                student = User.builder()
                        .name(dto.getName())
                        .accountId(dto.getAccountId())
                        .password(passwordEncoder.encode(dto.getPassword()))
                        .email(dto.getEmail())
                        .withdraw(false)
                        .build();
                userRepository.save(student);

                Role.getRoles(Role.USER).forEach(role ->
                        userRoleRepository.save(UserRole.builder()
                                .user(student)
                                .role(role)
                                .build()));
            } else {
                student = userRepository.findByAccountId(dto.getAccountId())
                        .orElseThrow(() -> new CustomException(CustomResponseException.NOT_FOUND_ACCOUNT));
            }

            if (!courseStudentRepository.existsByCourseAndUser(course, student)) {
                courseStudentRepository.save(CourseStudent.builder()
                        .course(course)
                        .user(student)
                        .inviteType(InviteType.FILE)
                        .build());
            } else {
                CourseStudent courseStudent = courseStudentRepository.findByCourseAndUser(course, student)
                        .orElseThrow(() -> new CustomException(CustomResponseException.NOT_FOUND_ACCOUNT));

                if (courseStudent.getInviteType() == InviteType.ONE)
                    courseStudentRepository.save(courseStudent.updateInviteType());
            }
        }
    }

    public Long kickStudent(Long userId, String courseUUid, String studentId) throws IOException {
        Course course = validateUtil.validateCourseOwner(userId, courseUUid);

        User student = userRepository.findByAccountId(studentId)
                .orElseThrow(() -> new CustomException(CustomResponseException.NOT_FOUND_ACCOUNT));

        CourseStudent courseStudent = courseStudentRepository.findByCourseAndUser(course, student)
                .orElseThrow(() -> new CustomException(CustomResponseException.NOT_COURSE_STUDENT));

        student.getCourseStudentList().remove(courseStudent);
        courseStudentRepository.delete(courseStudent);

        return course.getCourseId();
    }

    public List<CourseGradeDto> getCourseGrade(Long userId, String courseUUid) {
        Course course = validateUtil.validateCourseOwner(userId, courseUUid);

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
        Course course = validateUtil.validateCourseOwner(userId, courseUUid);

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

    public StudentInfoDTO getStudentInfo(Long userId, String accountId, String courseUUid) {
        Course course = validateUtil.validateCourseOwner(userId, courseUUid);

        User student = userRepository.findByAccountId(accountId)
                .orElseThrow(() -> new CustomException(CustomResponseException.NOT_FOUND_ACCOUNT));

        if (!courseStudentRepository.existsByCourseAndUser(course, student))
            throw new CustomException(CustomResponseException.NOT_COURSE_STUDENT);

        return StudentInfoDTO.builder()
                .accountId(student.getAccountId())
                .name(student.getName())
                .email(student.getEmail())
                .inviteType(courseStudentRepository.findByCourseAndUser(course, student).get().getInviteType())
                .build();
    }

}
