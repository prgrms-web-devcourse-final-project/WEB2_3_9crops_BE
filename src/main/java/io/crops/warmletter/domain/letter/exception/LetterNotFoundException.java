package io.crops.warmletter.domain.letter.exception;

import io.crops.warmletter.global.error.common.ErrorCode;
import io.crops.warmletter.global.error.exception.BusinessException;

public class LetterNotFoundException extends BusinessException {
    public LetterNotFoundException() {
        super(ErrorCode.LETTER_NOT_FOUND);
    }
}

