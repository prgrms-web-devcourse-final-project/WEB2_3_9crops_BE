package io.crops.warmletter.domain.member.exception;

import io.crops.warmletter.global.error.common.ErrorCode;
import io.crops.warmletter.global.error.exception.BusinessException;

public class DeletedMemberException extends BusinessException {

    public DeletedMemberException() {
        super(ErrorCode.DELETED_MEMBER);
    }
}
