package io.crops.warmletter.domain.auth.controller;

import io.crops.warmletter.domain.auth.dto.TokenResponse;
import io.crops.warmletter.domain.auth.service.AuthService;
import io.crops.warmletter.global.jwt.exception.InvalidAccessTokenException;
import io.crops.warmletter.global.response.BaseResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // Access 토큰이 만료되었을 때 호출
    @PostMapping("/reissue")
    public ResponseEntity<BaseResponse<TokenResponse>> reissue(
            @RequestHeader("Authorization") String bearerToken,
            @CookieValue(name = "refresh_token") String refreshToken,
            HttpServletResponse response
    ) {
        if (!bearerToken.startsWith("Bearer ")) {
            throw new InvalidAccessTokenException();
        }

        // Bearer 제거하고 토큰만 추출
        String accessToken = bearerToken.substring(7);

        TokenResponse tokenResponse = authService.reissue(accessToken, refreshToken, response);

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
