package io.crops.warmletter.domain.auth.service;

import io.crops.warmletter.domain.auth.dto.TokenResponse;
import io.crops.warmletter.domain.member.enums.Role;
import io.crops.warmletter.global.jwt.enums.TokenType;
import io.crops.warmletter.global.jwt.exception.InvalidRefreshTokenException;
import io.crops.warmletter.global.jwt.provider.JwtTokenProvider;
import io.crops.warmletter.global.jwt.service.TokenBlacklistService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;
    private final RedisTemplate<String, String> redisTemplate;
    private final long REFRESH_TOKEN_EXPIRE_TIME = 1000L * 60 * 60 * 24 * 14; // 14일
    private final long REFRESH_TOKEN_REISSUE_TIME = REFRESH_TOKEN_EXPIRE_TIME / 2; // 7일

    public TokenResponse reissue(String refreshToken, HttpServletResponse response) {
        // 리프레시 토큰 검증
        if (!jwtTokenProvider.validateToken(refreshToken, TokenType.REFRESH)) {
            throw new InvalidRefreshTokenException();
        }

        String email = jwtTokenProvider.getEmail(refreshToken);
        Claims claims = jwtTokenProvider.getClaims(refreshToken);

        String newAccessToken = jwtTokenProvider.createAccessToken(
                email,
                Role.valueOf(claims.get("role").toString()),
                claims.get("zipCode", String.class)
        );

        // Access Token을 Authorization 헤더에 추가
        response.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + newAccessToken);

        // 리프레시 토큰 만료가 임박한 경우 재발급
        if (jwtTokenProvider.getExpirationTime(refreshToken) < REFRESH_TOKEN_REISSUE_TIME) {
            String newRefreshToken = jwtTokenProvider.createRefreshToken(email);

            // Refresh Token을 쿠키에 저장
            ResponseCookie refreshTokenCookie = ResponseCookie.from("refresh_token", newRefreshToken)
                    .httpOnly(true)    // JavaScript에서 쿠키에 접근할 수 없도록 설정
//                .secure(true)      // HTTPS에서만 쿠키가 전송되도록 설정
//                .secure(false)     // 개발 환경에서는 HTTP만 허용, secure 관련 메서드를 사용하지 않으면 둘다 허용
                    .sameSite("Lax")   // CSRF 공격 방지를 위한 설정
                    .path("/")         // 쿠키가 유효한 경로 설정 ('/'는 모든 경로에서 사용 가능)
                    .maxAge(Duration.ofDays(14))  // 쿠키의 유효기간 설정 (14일)
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

            return new TokenResponse(newAccessToken, newRefreshToken);
        }

        return new TokenResponse(newAccessToken, refreshToken);
    }

    public void logout(String accessToken, String refreshToken, HttpServletResponse response) {
        // 토큰 무효화
        tokenBlacklistService.blacklistTokens(accessToken, refreshToken);

        // 리프레시 토큰 쿠키 제거
        Cookie cookie = new Cookie("refresh_token", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
//        cookie.setSecure(true);
        response.addCookie(cookie);
    }
}
