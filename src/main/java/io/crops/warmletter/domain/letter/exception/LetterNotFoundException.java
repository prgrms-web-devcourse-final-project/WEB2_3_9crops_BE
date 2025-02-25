package io.crops.warmletter.domain.letter.exception;

import io.crops.warmletter.global.error.common.ErrorCode;
import io.crops.warmletter.global.error.exception.BusinessException;

import static io.crops.warmletter.global.error.common.ErrorCode.LETTER_NOT_FOUND;

public class LetterNotFoundException extends BusinessException {
    public LetterNotFoundException() {
        super(LETTER_NOT_FOUND);
    }
}

