package io.crops.warmletter.domain.letter.exception;

import io.crops.warmletter.global.error.common.ErrorCode;
import io.crops.warmletter.global.error.exception.BusinessException;

public class MatchingNotFoundException extends BusinessException {

    public MatchingNotFoundException() {
        super(ErrorCode.MATCHING_NOT_FOUND);
    }
}
