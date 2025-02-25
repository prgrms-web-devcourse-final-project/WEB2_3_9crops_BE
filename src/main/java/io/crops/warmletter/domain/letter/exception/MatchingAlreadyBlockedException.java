package io.crops.warmletter.domain.letter.exception;

import io.crops.warmletter.global.error.common.ErrorCode;
import io.crops.warmletter.global.error.exception.BusinessException;

public class MatchingAlreadyBlockedException extends BusinessException {

    public MatchingAlreadyBlockedException() {
        super(ErrorCode.ALREADY_BLOCKED_MATCHING);
    }
}
