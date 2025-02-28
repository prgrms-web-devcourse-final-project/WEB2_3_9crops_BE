package io.crops.warmletter.domain.share.exception;

import io.crops.warmletter.global.error.common.ErrorCode;

public class SharePostNotFoundException extends ShareException {
    public SharePostNotFoundException() {
        super(ErrorCode.SHARE_POST_NOT_FOUND);
    }
}
