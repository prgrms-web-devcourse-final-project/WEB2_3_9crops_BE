package io.crops.warmletter.domain.eventpost.exception;

import io.crops.warmletter.global.error.common.ErrorCode;
import io.crops.warmletter.global.error.exception.BusinessException;

public class EventPostNotFoundException extends BusinessException {
    public EventPostNotFoundException() {
        super(ErrorCode.EVENT_POST_NOT_FOUND);
    }
}
