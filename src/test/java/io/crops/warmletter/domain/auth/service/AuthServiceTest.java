package io.crops.warmletter.domain.auth.service;

import io.crops.warmletter.domain.auth.dto.TokenResponse;
import io.crops.warmletter.domain.member.enums.Role;
import io.crops.warmletter.global.jwt.enums.TokenType;
import io.crops.warmletter.global.jwt.exception.InvalidRefreshTokenException;
import io.crops.warmletter.global.jwt.provider.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @DisplayName("리프레시 토큰 재발급 - 만료 임박하지 않은 경우")
    @Test
    void reissueToken_NotNearExpiration() {
        // given
        String refreshToken = "valid.refresh.token";
        String email = "test@example.com";
        String newAccessToken = "new.access.token";
        Claims claims = Mockito.mock(Claims.class);

        when(jwtTokenProvider.validateToken(refreshToken, TokenType.REFRESH)).thenReturn(true);
        when(jwtTokenProvider.getEmail(refreshToken)).thenReturn(email);
        when(jwtTokenProvider.getClaims(refreshToken)).thenReturn(claims);
        when(claims.get("role")).thenReturn("USER");
        when(claims.get("zipCode", String.class)).thenReturn("12345");
        when(jwtTokenProvider.createAccessToken(email, Role.USER, "12345")).thenReturn(newAccessToken);
        when(jwtTokenProvider.getExpirationTime(refreshToken)).thenReturn(1000L * 60 * 60 * 24 * 10); // 10일

        // when
        TokenResponse response = authService.reissue(refreshToken);

        // then
        assertThat(response.getAccessToken()).isEqualTo(newAccessToken);
        assertThat(response.getRefreshToken()).isEqualTo(refreshToken);
    }

    @DisplayName("리프레시 토큰 재발급 - 만료 임박한 경우")
    @Test
    void reissueToken_NearExpiration() {
        // given
        String refreshToken = "valid.refresh.token";
        String email = "test@example.com";
        String newAccessToken = "new.access.token";
        String newRefreshToken = "new.refresh.token";
        Claims claims = Mockito.mock(Claims.class);

        when(jwtTokenProvider.validateToken(refreshToken, TokenType.REFRESH)).thenReturn(true);
        when(jwtTokenProvider.getEmail(refreshToken)).thenReturn(email);
        when(jwtTokenProvider.getClaims(refreshToken)).thenReturn(claims);
        when(claims.get("role")).thenReturn("USER");
        when(claims.get("zipCode", String.class)).thenReturn("12345");
        when(jwtTokenProvider.createAccessToken(email, Role.USER, "12345")).thenReturn(newAccessToken);
        when(jwtTokenProvider.getExpirationTime(refreshToken)).thenReturn(1000L * 60 * 60 * 24 * 5); // 5일
        when(jwtTokenProvider.createRefreshToken(email)).thenReturn(newRefreshToken);

        // when
        TokenResponse response = authService.reissue(refreshToken);

        // then
        assertThat(response.getAccessToken()).isEqualTo(newAccessToken);
        assertThat(response.getRefreshToken()).isEqualTo(newRefreshToken);
    }

    @DisplayName("유효하지 않은 리프레시 토큰 검증 실패")
    @Test
    void reissueToken_InvalidToken() {
        // given
        String invalidRefreshToken = "invalid.refresh.token";
        when(jwtTokenProvider.validateToken(invalidRefreshToken, TokenType.REFRESH)).thenReturn(false);

        // when & then
        assertThrows(InvalidRefreshTokenException.class,
                () -> authService.reissue(invalidRefreshToken));
    }
}