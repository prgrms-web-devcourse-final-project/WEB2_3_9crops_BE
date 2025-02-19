package io.crops.warmletter.global.error.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // 공통 에러 코드
    INVALID_INPUT_VALUE("COM-001", HttpStatus.BAD_REQUEST, "잘못된 입력값입니다."),
    INTERNAL_SERVER_ERROR("COM-002", HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),

    // 페이징 관련 에러 코드
    INVALID_PAGE_REQUEST("PAGE-001", HttpStatus.BAD_REQUEST, "요청페이지 번호가 0보다 작습니다.")
    ;

    private final String code;
    private final HttpStatus status;
    private final String message;
}
