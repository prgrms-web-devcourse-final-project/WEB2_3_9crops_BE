package io.crops.warmletter.domain.eventpost.exception;

import io.crops.warmletter.global.error.common.ErrorCode;
import io.crops.warmletter.global.error.exception.BusinessException;

public class UsedEventPostNotFoundException extends BusinessException {
    public UsedEventPostNotFoundException() {
        super(ErrorCode.USED_EVENT_POST_NOT_FOUND);
    }
}
