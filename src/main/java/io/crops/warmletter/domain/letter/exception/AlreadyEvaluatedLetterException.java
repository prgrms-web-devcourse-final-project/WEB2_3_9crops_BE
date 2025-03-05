package io.crops.warmletter.domain.letter.exception;

import io.crops.warmletter.global.error.common.ErrorCode;
import io.crops.warmletter.global.error.exception.BusinessException;

public class AlreadyEvaluatedLetterException extends BusinessException {
    public AlreadyEvaluatedLetterException() {
        super(ErrorCode.ALREADY_EVALUATED_LETTER);
    }
}
