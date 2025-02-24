package io.crops.warmletter.domain.report.exception;

import io.crops.warmletter.global.error.common.ErrorCode;
import io.crops.warmletter.global.error.exception.BusinessException;

public class InvalidReportTargetException extends BusinessException {
    public InvalidReportTargetException() {
        super(ErrorCode.INVALID_REPORT_TARGET);
    }
}
