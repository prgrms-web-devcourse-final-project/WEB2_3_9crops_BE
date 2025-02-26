package io.crops.warmletter.domain.letter.exception;

import io.crops.warmletter.global.error.common.ErrorCode;
import io.crops.warmletter.global.error.exception.BusinessException;

import static io.crops.warmletter.global.error.common.ErrorCode.TEMPORARY_MATCHING_NOT_FOUND;

public class TemporaryMatchingNotFoundException extends BusinessException {
    public TemporaryMatchingNotFoundException() {
        super(TEMPORARY_MATCHING_NOT_FOUND);
    }
}
