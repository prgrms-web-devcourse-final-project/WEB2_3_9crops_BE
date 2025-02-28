package io.crops.warmletter.domain.letter.exception;

import io.crops.warmletter.global.error.exception.BusinessException;

import static io.crops.warmletter.global.error.common.ErrorCode.PARENT_LETTER_FOUND;

public class ParentLetterNotFoundException extends BusinessException {
    public ParentLetterNotFoundException() {
        super(PARENT_LETTER_FOUND);
    }
}
