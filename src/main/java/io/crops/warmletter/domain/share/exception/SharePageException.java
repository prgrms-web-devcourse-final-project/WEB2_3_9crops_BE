package io.crops.warmletter.domain.share.exception;

import io.crops.warmletter.global.error.common.ErrorCode;
import io.crops.warmletter.global.error.exception.BusinessException;

public class SharePageException extends BusinessException {
    public SharePageException() {
        super(ErrorCode.INVALID_PAGE_REQUEST);
    }
}
