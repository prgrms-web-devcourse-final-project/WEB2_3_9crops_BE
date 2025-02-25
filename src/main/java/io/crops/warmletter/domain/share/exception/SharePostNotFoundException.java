package io.crops.warmletter.domain.share.exception;

import io.crops.warmletter.global.error.common.ErrorCode;
import io.crops.warmletter.global.error.exception.BusinessException;

public class SharePostNotFoundException extends BusinessException {
    public SharePostNotFoundException() {
        super(ErrorCode.SHARE_POST_NOT_FOUND);
    }
}
