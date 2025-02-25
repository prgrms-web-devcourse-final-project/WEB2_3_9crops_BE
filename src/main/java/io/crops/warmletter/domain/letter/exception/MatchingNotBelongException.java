package io.crops.warmletter.domain.letter.exception;

import io.crops.warmletter.global.error.common.ErrorCode;
import io.crops.warmletter.global.error.exception.BusinessException;

public class MatchingNotBelongException extends BusinessException {

    public MatchingNotBelongException() {
        super(ErrorCode.NOT_BELONG_TO_MATCHING);
    }
}
