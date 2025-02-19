package io.crops.warmletter.global.error.exception;

import io.crops.warmletter.global.error.common.ErrorCode;
import lombok.Getter;

@Getter
public class OAuth2Exception extends RuntimeException {

    private final ErrorCode errorCode;

    public OAuth2Exception(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public OAuth2Exception(ErrorCode errorCode, String message) {
        super(message);  // 커스텀 메시지를 상위 클래스로 전달
        this.errorCode = errorCode;
    }
}
