package io.crops.warmletter.domain.badword.exception;

import io.crops.warmletter.global.error.common.ErrorCode;
import io.crops.warmletter.global.error.exception.BusinessException;

public class BadWordNotFoundException extends BusinessException {
    public BadWordNotFoundException() {
        super(ErrorCode.BAD_WORD_NOT_FOUND); // 에러코드 추가 필요
    }
}
