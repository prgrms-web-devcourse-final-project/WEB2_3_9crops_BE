package io.crops.warmletter.global.oauth.handler;

import io.crops.warmletter.global.oauth.entity.UserPrincipal;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomOAuth2AuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    @Value("${redirect.base.uri}")
    private String redirectUri;

    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {
        oAuth2AuthenticationSuccessHandler.onAuthenticationSuccess(request, response, null, authentication); // 기존 successHandler 호출

        // Access Token을 Authorization 헤더에서 가져오기
        String accessToken = response.getHeader(HttpHeaders.AUTHORIZATION);

        // 추가적인 로직 처리 (zipCode 유무에 따라 리다이렉션)
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        StringBuilder sb = new StringBuilder();
        sb.append(redirectUri);

        if (userPrincipal.getZipCode() == null || userPrincipal.getZipCode().isEmpty()) {
            sb.append("/onboarding"); // zipCode가 없으면 onboarding 페이지로
        } else {
            sb.append("/home"); // zipCode가 있으면 home 페이지로
        }
        sb.append("?accessToken=" + accessToken);

        String redirectUrl = sb.toString();

        getRedirectStrategy().sendRedirect(request, response, redirectUrl); // 리다이렉션
    }
}
