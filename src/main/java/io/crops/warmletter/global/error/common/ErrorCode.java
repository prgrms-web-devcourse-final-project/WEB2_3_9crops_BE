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

    // OAuth2 관련 에러 코드
    UNSUPPORTED_SOCIAL_LOGIN("AUTH-001", HttpStatus.BAD_REQUEST, "지원하지 않는 소셜 로그인입니다."),
    OAUTH2_PROCESSING_ERROR("AUTH-002", HttpStatus.INTERNAL_SERVER_ERROR, "소셜 로그인 처리 중 오류가 발생했습니다."),
    OAUTH2_EMAIL_NOT_FOUND("AUTH-003", HttpStatus.BAD_REQUEST, "소셜 계정에서 이메일을 찾을 수 없습니다.")
    ;

    private final String code;
    private final HttpStatus status;
    private final String message;
}
