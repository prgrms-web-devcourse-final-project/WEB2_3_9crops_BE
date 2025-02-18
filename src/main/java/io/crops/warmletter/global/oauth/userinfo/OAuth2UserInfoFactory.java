package io.crops.warmletter.global.oauth.userinfo;

import io.crops.warmletter.global.error.common.ErrorCode;
import io.crops.warmletter.global.oauth.exception.OAuth2AuthenticationProcessingException;

import java.util.Map;

public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        if(registrationId.equalsIgnoreCase("google")) {
            return new GoogleOAuth2UserInfo(attributes);
        } else if (registrationId.equalsIgnoreCase("kakao")) {
            return new KakaoOAuth2UserInfo(attributes);
        } else if (registrationId.equalsIgnoreCase("naver")) {
            return new NaverOAuth2UserInfo(attributes);
        } else {
            throw new OAuth2AuthenticationProcessingException(
                    String.format(ErrorCode.UNSUPPORTED_SOCIAL_LOGIN.getMessage() + " (%s)", registrationId));
        }
    }

}
