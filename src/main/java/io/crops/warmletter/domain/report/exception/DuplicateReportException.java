package io.crops.warmletter.domain.report.exception;


import io.crops.warmletter.global.error.common.ErrorCode;
import io.crops.warmletter.global.error.exception.BusinessException;

public class DuplicateReportException extends BusinessException {
    public DuplicateReportException() {
        super(ErrorCode.DUPLICATE_REPORT);
    }
}
