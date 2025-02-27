package io.crops.warmletter.domain.letter.exception;

import io.crops.warmletter.global.error.exception.BusinessException;

import static io.crops.warmletter.global.error.common.ErrorCode.DUPLICATE_LETTER_MATCH;

public class DuplicateLetterMatchException extends BusinessException {
    public DuplicateLetterMatchException() {
        super(DUPLICATE_LETTER_MATCH);
    }
}
