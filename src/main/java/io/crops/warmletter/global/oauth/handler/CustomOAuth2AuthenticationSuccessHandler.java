package io.crops.warmletter.global.oauth.handler;

import io.crops.warmletter.global.jwt.service.TokenStorage;
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
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CustomOAuth2AuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    @Value("${redirect.base.uri}")
    private String redirectUri;

    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final TokenStorage tokenStorage;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {
        oAuth2AuthenticationSuccessHandler.onAuthenticationSuccess(request, response, null, authentication); // 기존 successHandler 호출

        // Access Token을 Authorization 헤더에서 가져오기
        String accessToken = response.getHeader(HttpHeaders.AUTHORIZATION);
        if (accessToken != null && accessToken.startsWith("Bearer ")) {
            accessToken = accessToken.substring(7); // Bearer 부분 제거
        }

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        // 임시 상태 토큰 생성
        String stateToken = UUID.randomUUID().toString();

        // 토큰 저장소에 저장
        tokenStorage.storeTemporaryToken(stateToken, accessToken, userPrincipal);

        // 리다이렉션 URL 구성
        StringBuilder sb = new StringBuilder();
        sb.append(redirectUri);
        sb.append("/auth-callback?state=").append(stateToken);

        // 리다이렉션 대상 페이지 정보 추가
        if (userPrincipal.getZipCode() == null || userPrincipal.getZipCode().isEmpty()) {
            sb.append("&redirect=onboarding");
        } else {
            sb.append("&redirect=home");
        }

        getRedirectStrategy().sendRedirect(request, response, sb.toString());
    }
}
