package io.crops.warmletter.domain.badword.exception;

import io.crops.warmletter.global.error.common.ErrorCode;
import io.crops.warmletter.global.error.exception.BusinessException;

public class DuplicateBadWordException extends BusinessException {
    public DuplicateBadWordException() {
        super(ErrorCode.DUPLICATE_BANNED_WORD);
    }
}
