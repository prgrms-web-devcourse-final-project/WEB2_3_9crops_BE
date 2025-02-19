package io.crops.warmletter.global.oauth.exception;

import io.crops.warmletter.global.error.common.ErrorCode;
import io.crops.warmletter.global.error.exception.OAuth2Exception;

public class OAuth2ProcessingException extends OAuth2Exception {

    public OAuth2ProcessingException() {
        super(ErrorCode.OAUTH2_PROCESSING_ERROR);
    }
}
