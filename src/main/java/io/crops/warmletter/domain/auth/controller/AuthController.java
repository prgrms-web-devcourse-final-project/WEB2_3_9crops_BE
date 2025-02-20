package io.crops.warmletter.domain.auth.controller;

import io.crops.warmletter.domain.auth.dto.TokenResponse;
import io.crops.warmletter.domain.auth.service.AuthService;
import io.crops.warmletter.global.response.BaseResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // Access 토큰이 만료되었을 때 호출
    @PostMapping("/auth/reissue")
    public ResponseEntity<BaseResponse<TokenResponse>> reissue(
            @CookieValue(name = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        TokenResponse tokenResponse = authService.reissue(refreshToken);

        // Access Token을 Authorization 헤더에 추가
        response.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + tokenResponse.getAccessToken());

        // Refresh Token을 쿠키에 저장
        ResponseCookie refreshTokenCookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)    // JavaScript에서 쿠키에 접근할 수 없도록 설정
//                .secure(true)      // HTTPS에서만 쿠키가 전송되도록 설정
//                .secure(false)     // 개발 환경에서는 HTTP만 허용, secure 관련 메서드를 사용하지 않으면 둘다 허용
                .sameSite("Lax")   // CSRF 공격 방지를 위한 설정
                .path("/")         // 쿠키가 유효한 경로 설정 ('/'는 모든 경로에서 사용 가능)
                .maxAge(Duration.ofDays(14))  // 쿠키의 유효기간 설정 (14일)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

        return ResponseEntity.ok(BaseResponse.of(tokenResponse, "토큰 재발급 완료"));
    }
}
