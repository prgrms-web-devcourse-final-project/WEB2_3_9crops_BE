package io.crops.warmletter.domain.share.exception;

import io.crops.warmletter.global.error.common.ErrorCode;
import io.crops.warmletter.global.error.exception.BusinessException;

public class ShareAccessException extends BusinessException {
    public ShareAccessException() {
        super(ErrorCode.NOT_BELONG_TO_SHARE);
    }
}
