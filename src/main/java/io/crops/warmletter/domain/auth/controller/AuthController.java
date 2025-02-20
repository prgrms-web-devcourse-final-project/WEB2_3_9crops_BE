package io.crops.warmletter.domain.auth.controller;

import io.crops.warmletter.domain.auth.dto.TokenResponse;
import io.crops.warmletter.domain.auth.service.AuthService;
import io.crops.warmletter.global.jwt.service.TokenBlacklistService;
import io.crops.warmletter.global.response.BaseResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // Access 토큰이 만료되었을 때 호출
    @PostMapping("/reissue")
    public ResponseEntity<BaseResponse<TokenResponse>> reissue(
            @CookieValue(name = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        TokenResponse tokenResponse = authService.reissue(refreshToken, response);

        return ResponseEntity.ok(BaseResponse.of(tokenResponse, "토큰 재발급 완료"));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader("Authorization") String bearerToken,
            @CookieValue(value = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response) {

        if (refreshToken != null) {
            authService.logout(bearerToken.substring(7), refreshToken, response);
        }
        return ResponseEntity.ok().build();
    }
}
