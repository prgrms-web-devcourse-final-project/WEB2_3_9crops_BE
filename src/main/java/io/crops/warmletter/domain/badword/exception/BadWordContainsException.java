package io.crops.warmletter.domain.badword.exception;

import io.crops.warmletter.global.error.common.ErrorCode;
import io.crops.warmletter.global.error.exception.BusinessException;

public class BadWordContainsException extends BusinessException {
    public BadWordContainsException() {
        super(ErrorCode.BAD_WORD_CONTAINS);
    }
}

