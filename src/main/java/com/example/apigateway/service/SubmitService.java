package com.example.apigateway.service;

import com.example.apigateway.common.exception.CustomException;
import com.example.apigateway.common.exception.CustomResponseException;
import com.example.apigateway.common.kafka.KafkaSubmitForm;
import com.example.apigateway.common.type.Language;
import com.example.apigateway.entity.*;
import com.example.apigateway.form.result.ReceiveResultForm;
import com.example.apigateway.form.submit.SubmitForm;
import com.example.apigateway.repository.*;
import com.example.apigateway.service.common.ValidateUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Profile("submit")
@Service
@RequiredArgsConstructor
@Transactional
public class SubmitService {
    private final UserRepository userRepository;
    private final ProblemBankRepository problemBankRepository;
    private final ProblemRepository problemRepository;
    private final SubmitRepository submitRepository;
    private final ValidateUtil validateUtil;

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final ResultRepository resultRepository;

    public Long submitProblem(Long userId, String courseUUId, SubmitForm submitForm) throws IOException {
        Course course = validateUtil.validateCourseMember(userId, courseUUId);

        ProblemBank problemBank = problemBankRepository.findByCourse(course);

        Problem problem = problemRepository.findByProblemIdAndProblemBank(submitForm.getProblemId(), problemBank)
                .orElseThrow(() -> new CustomException(CustomResponseException.NOT_FOUND_PROBLEM));

        Submit submit = Submit.builder()
                .problem(problem)
                .student(userRepository.findById(userId)
                        .orElseThrow(() -> new CustomException(CustomResponseException.NOT_FOUND_ACCOUNT)))
                .code(submitForm.getCode())
                .language(Language.getLanguage(String.valueOf(submitForm.getLanguage())))
                .build();

        submitRepository.save(submit);

        String topic = "submission-" + submitForm.getLanguage();
        KafkaSubmitForm kafkaSubmitForm = KafkaSubmitForm.builder()
                .submitId(submit.getSubmitId())
                .problemId(problem.getProblemId())
                .userId(userId)
                .code(submitForm.getCode())
                .language(submitForm.getLanguage())
                .build();
        String payload = objectMapper.writeValueAsString(kafkaSubmitForm);

        kafkaTemplate.send(topic, payload);

        return problem.getProblemId();
    }

    public void receiveSubmitResult(ReceiveResultForm resultForm) {
        try {
            Submit submit = submitRepository.findById(resultForm.getSubmissionId())
                    .orElseThrow(() -> new CustomException(CustomResponseException.NOT_FOUND_SUBMIT));

            Result result = Result.builder()
                    .submit(submit)
                    .score(resultForm.getScore())
                    .status(resultForm.getStatus())
                    .executionTime(resultForm.getExecutionTime())
                    .errorDetail(resultForm.getErrorDetail())
                    .build();

            resultRepository.save(result);
        } catch (Exception e) {
            throw new CustomException(CustomResponseException.SERVER_ERROR);
        }
    }
}
