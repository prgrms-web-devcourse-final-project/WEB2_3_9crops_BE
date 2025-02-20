package io.crops.warmletter.domain.auth.service;

import io.crops.warmletter.domain.auth.dto.TokenResponse;
import io.crops.warmletter.domain.member.enums.Role;
import io.crops.warmletter.global.jwt.enums.TokenType;
import io.crops.warmletter.global.jwt.exception.InvalidRefreshTokenException;
import io.crops.warmletter.global.jwt.provider.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, String> redisTemplate;
    private final long REFRESH_TOKEN_EXPIRE_TIME = 1000L * 60 * 60 * 24 * 14; // 14일
    private final long REFRESH_TOKEN_REISSUE_TIME = REFRESH_TOKEN_EXPIRE_TIME / 2; // 7일

    public TokenResponse reissue(String refreshToken) {
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

        // 리프레시 토큰 만료가 임박한 경우 재발급
        if (jwtTokenProvider.getExpirationTime(refreshToken) < REFRESH_TOKEN_REISSUE_TIME) {
            String newRefreshToken = jwtTokenProvider.createRefreshToken(email);
            return new TokenResponse(newAccessToken, newRefreshToken);
        }

        return new TokenResponse(newAccessToken, refreshToken);
    }
}
