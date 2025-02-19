package io.crops.warmletter.global.oauth.exception;

import io.crops.warmletter.global.error.common.ErrorCode;
import io.crops.warmletter.global.error.exception.OAuth2Exception;

public class UnsupportedSocialTypeException extends OAuth2Exception {
    public UnsupportedSocialTypeException() {
        super(ErrorCode.UNSUPPORTED_SOCIAL_LOGIN);
    }

    public UnsupportedSocialTypeException(String provider) {
        super(ErrorCode.UNSUPPORTED_SOCIAL_LOGIN,
                String.format("%s 소셜 로그인은 지원하지 않습니다.", provider));
    }
}
