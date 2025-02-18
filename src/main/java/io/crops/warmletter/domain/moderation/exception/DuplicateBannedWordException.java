package io.crops.warmletter.domain.moderation.exception;

import io.crops.warmletter.global.error.common.ErrorCode;
import io.crops.warmletter.global.error.exception.BusinessException;

public class DuplicateBannedWordException extends BusinessException {
    public DuplicateBannedWordException() {
        super(ErrorCode.DUPLICATE_BANNED_WORD);
    }
}
