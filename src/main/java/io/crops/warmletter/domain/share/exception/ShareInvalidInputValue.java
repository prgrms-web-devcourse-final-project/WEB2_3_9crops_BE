package io.crops.warmletter.domain.share.exception;

import io.crops.warmletter.global.error.common.ErrorCode;
import io.crops.warmletter.global.error.exception.BusinessException;

public class ShareInvalidInputValue extends BusinessException {

  public ShareInvalidInputValue() {
    super(ErrorCode.INVALID_INPUT_VALUE);
  }
}
