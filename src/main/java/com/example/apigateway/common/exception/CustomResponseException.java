package com.example.apigateway.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CustomResponseException {

    // 200 error
    SUCCESS(HttpStatus.OK, "성공"),

    // 400 error
    WRONG_VALUE(HttpStatus.BAD_REQUEST, "잘못된 형식입니다."),
    NOT_FOUND_ACCOUNT(HttpStatus.BAD_REQUEST,"존해하지 않는 계정입니다."),
    WRONG_PASSWORD(HttpStatus.BAD_REQUEST, "패스워드가 틀렸습니다."),
    WITHDRAW_USER(HttpStatus.BAD_REQUEST, "탈퇴한 회원입니다."),
    NOT_REGISTERED_EMAIL(HttpStatus.BAD_REQUEST, "이메일이 등록되어 있지 않습니다."),
    INVALID_CODE(HttpStatus.BAD_REQUEST, "인증 코드가 유효하지 않습니다."),
    CERTIFICATION_EXPIRED(HttpStatus.BAD_REQUEST, "인증 시간이 만료되었습니다."),
    NOT_FOUND_COURSE(HttpStatus.BAD_REQUEST, "존재하지 않는 강의입니다."),
    WRONG_FILE_TYPE(HttpStatus.BAD_REQUEST, "잘못된 파일 형식입니다."),
    NOT_COURSE_STUDENT(HttpStatus.BAD_REQUEST, "강의에 존재하지 않는 학생입니다."),
    FILE_NOT_FOUND(HttpStatus.BAD_REQUEST, "파일을 찾을 수 없습니다."),
    NOT_FOUND_PROBLEM(HttpStatus.BAD_REQUEST, "문제를 찾을 수 없습니다."),
    INVALID_TESTCASE(HttpStatus.BAD_REQUEST, "잘못된 테스트케이스입니다."),
    INVALID_EXCEL_FILE(HttpStatus.BAD_REQUEST, "잘못된 엑셀 파일입니다."),
    INVALID_TEST_CASE_FILE(HttpStatus.BAD_REQUEST, "잘못된 테스트케이스 파일입니다."),
    UNKNOWN_LANGUAGE(HttpStatus.BAD_REQUEST, "알 수 없는 언어입니다."),
    NOT_FOUND_SUBMIT(HttpStatus.BAD_REQUEST, "제출을 찾을 수 없습니다."),
    NOT_SCORE_YET(HttpStatus.BAD_REQUEST, "점수가 아직 채점되지 않았습니다."),
    DUPLICATE_ACCOUNT(HttpStatus.BAD_REQUEST, "중복된 계정입니다."),

    // 401 error
    TOKEN_HAS_EXPIRED(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "토큰이 유효하지 않습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "재발급 토큰이 유효하지 않습니다."),
    NOT_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "재발급 토큰이 존재하지 않습니다."),

    // 403 error
    FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없습니다."),

    // 500 error
    SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류"),
    IOE_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, "입출력 오류"), ;

    private final HttpStatus httpStatus;
    private final String message;
}
