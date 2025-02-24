package io.crops.warmletter.domain.report.exception;

import io.crops.warmletter.global.error.common.ErrorCode;
import io.crops.warmletter.global.error.exception.BusinessException;

public class InvalidReportRequestException extends BusinessException {
    public InvalidReportRequestException() {
        super(ErrorCode.INVALID_REPORT_REQUEST);
    }
}
