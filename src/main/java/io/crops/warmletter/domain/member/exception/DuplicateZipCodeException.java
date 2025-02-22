package io.crops.warmletter.domain.member.exception;

import io.crops.warmletter.global.error.common.ErrorCode;
import io.crops.warmletter.global.error.exception.BusinessException;

public class DuplicateZipCodeException extends BusinessException {

    public DuplicateZipCodeException() {
        super(ErrorCode.DUPLICATE_ZIP_CODE);
    }
}
