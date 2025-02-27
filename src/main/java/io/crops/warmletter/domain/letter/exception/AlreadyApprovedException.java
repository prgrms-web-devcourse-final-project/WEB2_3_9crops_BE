package io.crops.warmletter.domain.letter.exception;

import io.crops.warmletter.global.error.exception.BusinessException;

import static io.crops.warmletter.global.error.common.ErrorCode.ALREADY_APPROVED;

public class AlreadyApprovedException extends BusinessException {
    public AlreadyApprovedException() {
        super(ALREADY_APPROVED);
    }
}
