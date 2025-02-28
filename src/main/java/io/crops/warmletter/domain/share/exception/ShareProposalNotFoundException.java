package io.crops.warmletter.domain.share.exception;

import io.crops.warmletter.global.error.common.ErrorCode;
import io.crops.warmletter.global.error.exception.BusinessException;

public class ShareProposalNotFoundException extends BusinessException {
    public ShareProposalNotFoundException() {
        super(ErrorCode.SHARE_PROPOSAL_NOTFOUND);
    }
}
