package io.crops.warmletter.domain.auth.exception;

import io.crops.warmletter.global.error.common.ErrorCode;
import io.crops.warmletter.global.error.exception.AuthException;

public class UnauthorizedException extends AuthException {

    public UnauthorizedException() {
        super(ErrorCode.UNAUTHORIZED);
    }
}
