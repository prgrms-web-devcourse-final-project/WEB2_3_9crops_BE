package io.crops.warmletter.global.jwt.exception;

import io.crops.warmletter.global.error.common.ErrorCode;
import io.crops.warmletter.global.error.exception.JwtAuthenticationException;

public class InvalidTokenException extends JwtAuthenticationException {

    public InvalidTokenException() {
        super(ErrorCode.INVALID_TOKEN);
    }
}
