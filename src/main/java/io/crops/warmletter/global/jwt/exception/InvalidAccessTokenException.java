package io.crops.warmletter.global.jwt.exception;

import io.crops.warmletter.global.error.common.ErrorCode;
import io.crops.warmletter.global.error.exception.JwtAuthenticationException;

public class InvalidAccessTokenException extends JwtAuthenticationException {

    public InvalidAccessTokenException() {
        super(ErrorCode.INVALID_ACCESS_TOKEN);
    }
}
