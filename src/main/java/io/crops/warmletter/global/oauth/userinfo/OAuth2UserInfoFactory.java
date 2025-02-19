package io.crops.warmletter.global.oauth.userinfo;

import io.crops.warmletter.global.error.common.ErrorCode;
import io.crops.warmletter.global.error.exception.OAuth2Exception;
import io.crops.warmletter.global.oauth.exception.UnsupportedSocialTypeException;

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
            throw new UnsupportedSocialTypeException();
        }
    }

}
