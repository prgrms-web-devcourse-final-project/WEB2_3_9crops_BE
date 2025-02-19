package io.crops.warmletter.global.oauth.exception;

import io.crops.warmletter.global.error.common.ErrorCode;
import io.crops.warmletter.global.error.exception.OAuth2Exception;

public class OAuth2EmailNotFoundException extends OAuth2Exception {
    public OAuth2EmailNotFoundException() {
        super(ErrorCode.OAUTH2_EMAIL_NOT_FOUND);  // ErrorCode 추가 필요
    }

    public OAuth2EmailNotFoundException(String provider) {
        super(ErrorCode.OAUTH2_EMAIL_NOT_FOUND,
                String.format("%s 계정에서 이메일을 찾을 수 없습니다.", provider));
    }
}
