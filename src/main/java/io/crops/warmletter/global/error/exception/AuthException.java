package io.crops.warmletter.global.error.exception;

import io.crops.warmletter.global.error.common.ErrorCode;
import lombok.Getter;

@Getter
public class AuthException extends RuntimeException {

    private final ErrorCode errorCode;

    public AuthException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
