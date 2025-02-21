package io.crops.warmletter.global.jwt.exception;

import io.crops.warmletter.global.error.common.ErrorCode;
import io.crops.warmletter.global.error.exception.JwtAuthenticationException;

public class InvalidRefreshTokenException extends JwtAuthenticationException {

    public InvalidRefreshTokenException() {
        super(ErrorCode.INVALID_REFRESH_TOKEN);
    }
}
