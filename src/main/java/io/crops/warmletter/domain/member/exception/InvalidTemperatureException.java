package io.crops.warmletter.domain.member.exception;

import io.crops.warmletter.global.error.common.ErrorCode;
import io.crops.warmletter.global.error.exception.BusinessException;

public class InvalidTemperatureException extends BusinessException {

    public InvalidTemperatureException() {
        super(ErrorCode.INVALID_TEMPERATURE);
    }
}
