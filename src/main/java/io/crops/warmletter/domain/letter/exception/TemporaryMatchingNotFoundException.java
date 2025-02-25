package io.crops.warmletter.domain.letter.exception;

import io.crops.warmletter.global.error.common.ErrorCode;
import io.crops.warmletter.global.error.exception.BusinessException;

public class TemporaryMatchingNotFoundException extends BusinessException {
    public TemporaryMatchingNotFoundException() {
        super(ErrorCode.TEMPORARY_MATCHING_NOT_FOUND);
    }
}
