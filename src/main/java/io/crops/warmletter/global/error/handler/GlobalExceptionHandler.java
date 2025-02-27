package io.crops.warmletter.global.error.handler;

import io.crops.warmletter.global.error.common.ErrorCode;
import io.crops.warmletter.global.error.exception.AuthException;
import io.crops.warmletter.global.error.exception.BusinessException;
import io.crops.warmletter.global.error.exception.JwtAuthenticationException;
import io.crops.warmletter.global.error.response.ErrorResponse;
import io.crops.warmletter.global.error.exception.OAuth2Exception;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // validation 관련
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e) {
        log.error("Validation error occurred: {}", e.getMessage(), e);
        String message = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();

        return createErrorResponse(ErrorCode.INVALID_INPUT_VALUE, message);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        log.error("Business error occurred: {}", e.getMessage(), e);
        return createErrorResponse(e.getErrorCode());
    }

    // DB 관련 예외 처리
    @ExceptionHandler(DataAccessException.class)
    protected ResponseEntity<ErrorResponse> handleDataAccessException(DataAccessException e) {
        log.error("Database error occurred: {}", e.getMessage(), e);
        return createErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    // JSON 파싱 실패 시 (잘못된 데이터 형식) 처리
    // enum 값이 잘못된 경우
    // 날짜 형식이 잘못된 경우
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException e) {
        log.error("JSON parsing error occurred: {}", e.getMessage(), e);
        return createErrorResponse(ErrorCode.INVALID_INPUT_VALUE);
    }

    // oauth 관련 에러
    @ExceptionHandler(OAuth2Exception.class)
    public ResponseEntity<ErrorResponse> handleOAuth2Exception(
            OAuth2Exception e) {
        log.error("OAuth2 authentication error occurred: {}", e.getMessage(), e);
        return createErrorResponse(ErrorCode.OAUTH2_PROCESSING_ERROR, e.getMessage());
    }

    // Jwt 관련 에러
    @ExceptionHandler(JwtAuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleJwtAuthenticationException(
            JwtAuthenticationException e) {
        log.error("JWT authentication error occurred: {}", e.getMessage(), e);
        return createErrorResponse(e.getErrorCode());
    }

    // Auth 관련 에러
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ErrorResponse> handleAuthException(
            JwtAuthenticationException e) {
        log.error("Auth error occurred: {}", e.getMessage(), e);
        return createErrorResponse(e.getErrorCode());
    }

    // 쿠키가 필수인데 없을 때
    @ExceptionHandler(MissingRequestCookieException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestCookieException(MissingRequestCookieException e) {
        log.error("Cookie error occurred: {}", e.getMessage(), e);
        return createErrorResponse(ErrorCode.INVALID_TOKEN);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unexpected error occurred: {}", e.getMessage(), e);
        return createErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ErrorResponse> createErrorResponse(ErrorCode errorCode) {
        return ResponseEntity.status(errorCode.getStatus())
                .body(new ErrorResponse(errorCode.getCode(), errorCode.getMessage()));
    }

    private ResponseEntity<ErrorResponse> createErrorResponse(ErrorCode errorCode, String message) {
        return ResponseEntity.status(errorCode.getStatus())
                .body(new ErrorResponse(errorCode.getCode(), message));
    }
}
