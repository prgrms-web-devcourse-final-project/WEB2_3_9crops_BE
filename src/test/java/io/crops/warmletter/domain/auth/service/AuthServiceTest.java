package io.crops.warmletter.domain.auth.service;

import io.crops.warmletter.domain.auth.dto.TokenResponse;
import io.crops.warmletter.domain.auth.dto.TokenStorageResponse;
import io.crops.warmletter.domain.auth.exception.UnauthorizedException;
import io.crops.warmletter.domain.member.enums.Role;
import io.crops.warmletter.global.jwt.enums.TokenType;
import io.crops.warmletter.global.jwt.exception.InvalidRefreshTokenException;
import io.crops.warmletter.global.jwt.exception.InvalidTokenException;
import io.crops.warmletter.global.jwt.provider.JwtTokenProvider;
import io.crops.warmletter.global.jwt.service.TokenBlacklistService;
import io.crops.warmletter.global.jwt.service.TokenStorage;
import io.crops.warmletter.global.oauth.entity.UserPrincipal;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private HttpServletResponse response;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private TokenStorage tokenStorage;

    @DisplayName("리프레시 토큰 재발급 - 만료 임박하지 않은 경우")
    @Test
    void reissueToken_NotNearExpiration() {
        // given
        String accessToken = "valid.access.token";
        String refreshToken = "valid.refresh.token";
        String socialUniqueId = "GOOGLE_12345";
        String newAccessToken = "new.access.token";
        Long memberId = 1L;
        Claims claims = Mockito.mock(Claims.class);

        when(jwtTokenProvider.validateToken(refreshToken, TokenType.REFRESH)).thenReturn(true);
        when(jwtTokenProvider.getSocialUniqueId(accessToken)).thenReturn(socialUniqueId);
        when(jwtTokenProvider.getClaims(accessToken)).thenReturn(claims);
        when(claims.get("role")).thenReturn("USER");
        when(claims.get("zipCode", String.class)).thenReturn("12345");
        when(claims.get("memberId", Long.class)).thenReturn(memberId);
        when(jwtTokenProvider.createAccessToken(socialUniqueId, Role.USER, "12345", memberId)).thenReturn(newAccessToken);
        when(jwtTokenProvider.getExpirationTime(refreshToken)).thenReturn(1000L * 60 * 60 * 24 * 10); // 10일

        // when
        TokenResponse response = authService.reissue(accessToken, refreshToken, this.response);

        // then
        assertThat(response.getAccessToken()).isEqualTo(newAccessToken);
        assertThat(response.getRefreshToken()).isEqualTo(refreshToken);
    }

    @DisplayName("리프레시 토큰 재발급 - 만료 임박한 경우")
    @Test
    void reissueToken_NearExpiration() {
        // given
        String accessToken = "valid.access.token";
        String refreshToken = "valid.refresh.token";
        String socialUniqueId = "GOOGLE_12345";
        String newAccessToken = "new.access.token";
        String newRefreshToken = "new.refresh.token";
        Long memberId = 1L;
        Claims claims = Mockito.mock(Claims.class);

        when(jwtTokenProvider.validateToken(refreshToken, TokenType.REFRESH)).thenReturn(true);
        when(jwtTokenProvider.getSocialUniqueId(accessToken)).thenReturn(socialUniqueId);
        when(jwtTokenProvider.getClaims(accessToken)).thenReturn(claims);
        when(claims.get("role")).thenReturn("USER");
        when(claims.get("zipCode", String.class)).thenReturn("12345");
        when(claims.get("memberId", Long.class)).thenReturn(1L);
        when(jwtTokenProvider.createAccessToken(socialUniqueId, Role.USER, "12345", memberId)).thenReturn(newAccessToken);
        when(jwtTokenProvider.getExpirationTime(refreshToken)).thenReturn(1000L * 60 * 60 * 24 * 5); // 5일
        when(jwtTokenProvider.createRefreshToken(socialUniqueId)).thenReturn(newRefreshToken);

        // when
        TokenResponse response = authService.reissue(accessToken, refreshToken, this.response);

        // then
        assertThat(response.getAccessToken()).isEqualTo(newAccessToken);
        assertThat(response.getRefreshToken()).isEqualTo(newRefreshToken);
    }

    @DisplayName("유효하지 않은 리프레시 토큰 검증 실패")
    @Test
    void reissueToken_InvalidToken() {
        // given
        String invalidAccessToken = "invalid.access.token";
        String invalidRefreshToken = "invalid.refresh.token";
        when(jwtTokenProvider.validateToken(invalidRefreshToken, TokenType.REFRESH)).thenReturn(false);

        // when & then
        assertThrows(InvalidRefreshTokenException.class,
                () -> authService.reissue(invalidAccessToken, invalidRefreshToken, this.response));
    }

    @Test
    @DisplayName("로그아웃 성공")
    void logout_Success() {
        // given
        String accessToken = "valid.access.token";
        String refreshToken = "valid.refresh.token";
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        // when
        authService.logout(accessToken, refreshToken, mockResponse);

        // then
        verify(tokenBlacklistService).blacklistTokens(accessToken, refreshToken);

        // 일반 Cookie 대신 ResponseCookie 사용
        ResponseCookie expectedCookie = ResponseCookie.from("refresh_token", null)
                .maxAge(0)
                .path("/")
                .httpOnly(true)
                .build();

        String setCookieHeader = mockResponse.getHeader(HttpHeaders.SET_COOKIE);
        assertThat(setCookieHeader).isEqualTo(expectedCookie.toString());
    }

    @Test
    @DisplayName("현재 사용자 정보 조회 성공")
    void getCurrentUser_Success() {
        // given
        UserPrincipal userPrincipal = UserPrincipal.builder()
                .id(1L)
                .socialUniqueId("GOOGLE_12345")
                .role(Role.USER)
                .zipCode("12345")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .build();

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userPrincipal, null, userPrincipal.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // when
        UserPrincipal currentUser = authService.getCurrentUser();

        // then
        assertThat(currentUser).isEqualTo(userPrincipal);
    }

    @Test
    @DisplayName("현재 사용자 정보 조회 실패 - 인증 정보 없음")
    void getCurrentUser_Unauthorized() {
        // given
        SecurityContextHolder.clearContext();

        // when & then
        assertThrows(UnauthorizedException.class, () -> authService.getCurrentUser());
    }

    @Test
    @DisplayName("현재 사용자 ID 조회")
    void getCurrentUserId_Success() {
        // given
        Long userId = 1L;
        UserPrincipal userPrincipal = UserPrincipal.builder()
                .id(userId)
                .socialUniqueId("GOOGLE_12345")
                .role(Role.USER)
                .zipCode("12345")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .build();

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userPrincipal, null, userPrincipal.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // when
        Long currentUserId = authService.getCurrentUserId();

        // then
        assertThat(currentUserId).isEqualTo(userId);
    }

    @Test
    @DisplayName("현재 사용자 우편번호 조회")
    void getZipCode_Success() {
        // given
        String zipCode = "12345";
        UserPrincipal userPrincipal = UserPrincipal.builder()
                .id(1L)
                .socialUniqueId("GOOGLE_12345")
                .role(Role.USER)
                .zipCode(zipCode)
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .build();

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userPrincipal, null, userPrincipal.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // when
        String currentZipCode = authService.getZipCode();

        // then
        assertThat(currentZipCode).isEqualTo(zipCode);
    }

    @DisplayName("토큰 스토리지 조회 실패 - 토큰 정보 없음")
    @Test
    void getTokenByState_WithNotFoundTokenInfo_ShouldThrowException() {
        //given
        String invalidStateToken = "invalid.state.token";

        when(tokenStorage.getTokenInfo(invalidStateToken)).thenReturn(null);
        //when & then
        assertThrows(InvalidTokenException.class,
                () -> authService.getTokenByState(invalidStateToken));

        verify(tokenStorage).getTokenInfo(invalidStateToken);
    }

    @DisplayName("토큰 스토리지 조회 성공 - zipCode 존재")
    @Test
    void getTokenByState_Success_ExistZipCode() {
        //given
        String validStateToken = "valid.state.token";
        String accessToken = "access.token";
        Long memberId = 1L;
        String zipCode = "1A2A4";
        long createdAt = System.currentTimeMillis();
        UserPrincipal userInfo = UserPrincipal.builder()
                .id(memberId)
                .zipCode(zipCode)
                .build();

        TokenStorage.TokenInfo validTokenInfo = new TokenStorage.TokenInfo(accessToken, userInfo, createdAt);

        when(tokenStorage.getTokenInfo(validStateToken)).thenReturn(validTokenInfo);

        //when
        TokenStorageResponse tokenResponse = authService.getTokenByState(validStateToken);

        //then
        assertThat(accessToken).isEqualTo(tokenResponse.getAccessToken());
        assertThat(memberId).isEqualTo(tokenResponse.getUserId());
        assertThat(tokenResponse.isHasZipCode()).isTrue();

        verify(tokenStorage).getTokenInfo(validStateToken);
    }

    @DisplayName("토큰 스토리지 조회 성공 - zipCode 존재 안함")
    @Test
    void getTokenByState_Success_NotExistZipCode() {
        //given
        String validStateToken = "valid.state.token";
        String accessToken = "access.token";
        Long memberId = 1L;
        long createdAt = System.currentTimeMillis();
        UserPrincipal userInfo = UserPrincipal.builder()
                .id(memberId)
                .zipCode(null)
                .build();

        TokenStorage.TokenInfo validTokenInfo = new TokenStorage.TokenInfo(accessToken, userInfo, createdAt);

        when(tokenStorage.getTokenInfo(validStateToken)).thenReturn(validTokenInfo);

        //when
        TokenStorageResponse tokenResponse = authService.getTokenByState(validStateToken);

        //then
        assertThat(accessToken).isEqualTo(tokenResponse.getAccessToken());
        assertThat(memberId).isEqualTo(tokenResponse.getUserId());
        assertThat(tokenResponse.isHasZipCode()).isFalse();

        verify(tokenStorage).getTokenInfo(validStateToken);
    }
}