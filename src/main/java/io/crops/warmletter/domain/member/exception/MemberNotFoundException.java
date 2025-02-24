package io.crops.warmletter.domain.member.exception;

import io.crops.warmletter.global.error.common.ErrorCode;
import io.crops.warmletter.global.error.exception.BusinessException;

public class MemberNotFoundException extends BusinessException {

    public MemberNotFoundException() {
        super(ErrorCode.MEMBER_NOT_FOUND);
    }
}
