package io.crops.warmletter.domain.share.exception;

import io.crops.warmletter.global.error.common.ErrorCode;
import io.crops.warmletter.global.error.exception.BusinessException;

public class ShareException extends BusinessException {
    public ShareException(ErrorCode errorCode) {
        super(errorCode);
    }
}
