package io.crops.warmletter.domain.eventpost.exception;

import io.crops.warmletter.global.error.common.ErrorCode;
import io.crops.warmletter.global.error.exception.BusinessException;

public class EventCommentNotFoundException extends BusinessException {
    public EventCommentNotFoundException() {
        super(ErrorCode.EVENT_COMMENT_NOT_FOUND);
    }
}
