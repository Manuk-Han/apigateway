package com.example.apigateway.service;

import com.example.apigateway.common.exception.CustomException;
import com.example.apigateway.common.exception.CustomResponseException;
import com.example.apigateway.dto.result.ResultDetailDto;
import com.example.apigateway.dto.result.ResultDto;
import com.example.apigateway.entity.Course;
import com.example.apigateway.entity.Problem;
import com.example.apigateway.entity.Result;
import com.example.apigateway.entity.User;
import com.example.apigateway.form.result.FeedbackForm;
import com.example.apigateway.repository.ProblemRepository;
import com.example.apigateway.repository.ResultRepository;
import com.example.apigateway.repository.SubmitRepository;
import com.example.apigateway.repository.UserRepository;
import com.example.apigateway.service.common.ValidateUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Profile("course")
@Service
@RequiredArgsConstructor
public class ResultService {
    private final UserRepository userRepository;
    private final ProblemRepository problemRepository;
    private final SubmitRepository submitRepository;
    private final ValidateUtil validateUtil;

    private final ResultRepository resultRepository;

    public List<ResultDto> getResultList(Long userId, String courseUUId, Long problemId) {
        Course course = validateUtil.validateCourseMember(userId, courseUUId);

        Problem problem = problemRepository.findByProblemIdAndProblemBank(problemId, course.getProblemBank())
                .orElseThrow(() -> new CustomException(CustomResponseException.NOT_FOUND_PROBLEM));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(CustomResponseException.NOT_FOUND_ACCOUNT));

        List<ResultDto> resultDtoList = new ArrayList<>();

        submitRepository.findSubmitsByProblemAndStudent(problem, user).stream().toList()
                .forEach(submit -> {
                    Result result = resultRepository.findResultBySubmit(submit)
                            .orElseThrow(() -> new CustomException(CustomResponseException.NOT_SCORE_YET));

                    ResultDto resultDto = ResultDto.builder()
                            .resultId(result.getResultId())
                            .submitId(submit.getSubmitId())
                            .status(result.getStatus())
                            .score(result.getScore())
                            .language(submit.getLanguage())
                            .submitTime(submit.getSubmitTime())
                            .accountId(user.getAccountId())
                            .build();

                    resultDtoList.add(resultDto);
                });

        return resultDtoList;
    }

    public List<ResultDto> getResultListForClassOwner(Long userId, String courseUUId, Long problemId) {
        Course course = validateUtil.validateCourseOwner(userId, courseUUId);

        Problem problem = problemRepository.findByProblemIdAndProblemBank(problemId, course.getProblemBank())
                .orElseThrow(() -> new CustomException(CustomResponseException.NOT_FOUND_PROBLEM));

        List<ResultDto> resultDtoList = new ArrayList<>();

        submitRepository.findSubmitsByProblem(problem).stream().toList()
                .forEach(submit -> {
                    Result result = resultRepository.findResultBySubmit(submit)
                            .orElseThrow(() -> new CustomException(CustomResponseException.NOT_SCORE_YET));

                    ResultDto resultDto = ResultDto.builder()
                            .resultId(result.getResultId())
                            .submitId(submit.getSubmitId())
                            .status(result.getStatus())
                            .score(result.getScore())
                            .language(submit.getLanguage())
                            .submitTime(submit.getSubmitTime())
                            .accountId(submit.getStudent().getAccountId())
                            .build();

                    resultDtoList.add(resultDto);
                });

        return resultDtoList;
    }

    public ResultDetailDto getResultDetail(Long userId, String courseUUId, Long resultId) {
        validateUtil.validateCourseMember(userId, courseUUId);

        Result result = resultRepository.findById(resultId)
                .orElseThrow(() -> new CustomException(CustomResponseException.NOT_SCORE_YET));

        return ResultDetailDto.builder()
                .resultId(result.getResultId())
                .submitId(result.getSubmit().getSubmitId())
                .status(result.getStatus())
                .score(result.getScore())
                .language(result.getSubmit().getLanguage())
                .submitTime(result.getSubmit().getSubmitTime())
                .accountId(result.getSubmit().getStudent().getAccountId())
                .errorMessage(result.getErrorDetail())
                .feedback(result.getFeedback())
                .build();
    }

    public Long setFeedback(Long userId, String courseUUId, FeedbackForm feedbackForm) {
        validateUtil.validateCourseOwner(userId, courseUUId);

        Result result = resultRepository.findById(feedbackForm.getResultId())
                .orElseThrow(() -> new CustomException(CustomResponseException.NOT_SCORE_YET));

        result.setFeedback(feedbackForm.getFeedback());
        resultRepository.save(result);

        return result.getResultId();
    }
}
