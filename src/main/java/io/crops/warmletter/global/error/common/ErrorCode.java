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

    // 공유 관련 에러 코드
    INVALID_PAGE_REQUEST("SHARE-001", HttpStatus.BAD_REQUEST, "요청페이지 번호가 0보다 작습니다."),
    SHARE_POST_NOT_FOUND("SHARE-002",HttpStatus.NOT_FOUND,"해당 공유 게시글을 찾을 수 없습니다."),
    SHARE_PROPOSAL_NOTFOUND("SHARE-003",HttpStatus.NOT_FOUND,"해당 공유 제안을 찾을 수 없습니다."),

    // 이벤트 게시판 관련 에러 코드
    EVENT_POST_NOT_FOUND("EVENT-001",HttpStatus.NOT_FOUND,"해당 이벤트 게시글을 찾을 수 없습니다."),
    USED_EVENT_POST_NOT_FOUND("EVENT-002",HttpStatus.NOT_FOUND,"사용중인 이벤트 게시글을 찾을 수 없습니다."),
    EVENT_COMMENT_NOT_FOUND("EVENT-003",HttpStatus.NOT_FOUND,"해당 이벤트 게시글의 댓글을 찾을 수 없습니다."),

    //금칙어
    DUPLICATE_BANNED_WORD("MOD-001", HttpStatus.CONFLICT, "이미 등록된 금칙어입니다."),
    BAD_WORD_NOT_FOUND("MOD-002", HttpStatus.NOT_FOUND, "해당 금칙어가 존재하지 않습니다."),
    BAD_WORD_CONTAINS("MOD-003", HttpStatus.BAD_REQUEST, "금칙어가 포함되어 있습니다."),


    // OAuth2 관련 에러 코드
    UNSUPPORTED_SOCIAL_LOGIN("AUTH-001", HttpStatus.BAD_REQUEST, "지원하지 않는 소셜 로그인입니다."),
    OAUTH2_PROCESSING_ERROR("AUTH-002", HttpStatus.INTERNAL_SERVER_ERROR, "소셜 로그인 처리 중 오류가 발생했습니다."),
    OAUTH2_EMAIL_NOT_FOUND("AUTH-003", HttpStatus.BAD_REQUEST, "소셜 계정에서 이메일을 찾을 수 없습니다."),
    INVALID_REFRESH_TOKEN("AUTH-004", HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다."),
    INVALID_ACCESS_TOKEN("AUTH-005", HttpStatus.UNAUTHORIZED, "유효하지 않은 엑세스 토큰입니다."),
    INVALID_TOKEN("AUTH-006", HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    UNAUTHORIZED("AUTH-007", HttpStatus.UNAUTHORIZED, "인증 정보가 없습니다."),


    //Letter Error code
    LETTER_NOT_FOUND("LET-001", HttpStatus.NOT_FOUND, "해당 편지를 찾을 수 없습니다."),

    // Member 관련
    MEMBER_NOT_FOUND("MEM-001", HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."),
    DUPLICATE_ZIP_CODE("MEM-002", HttpStatus.CONFLICT, "우편번호가 이미 존재합니다."),
    ;

    private final String code;
    private final HttpStatus status;
    private final String message;
}
