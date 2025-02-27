package io.crops.warmletter.domain.report.exception;

import io.crops.warmletter.global.error.common.ErrorCode;
import io.crops.warmletter.global.error.exception.BusinessException;

public class ReportNotFoundException extends BusinessException {
    public ReportNotFoundException() {
        super(ErrorCode.BAD_WORD_NOT_FOUND);
    }
}
