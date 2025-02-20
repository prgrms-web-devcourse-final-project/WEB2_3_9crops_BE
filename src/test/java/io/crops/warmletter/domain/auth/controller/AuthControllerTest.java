package io.crops.warmletter.domain.auth.controller;

import io.crops.warmletter.domain.auth.dto.TokenResponse;
import io.crops.warmletter.domain.auth.service.AuthService;
import io.crops.warmletter.global.config.CorsConfig;
import io.crops.warmletter.global.config.TestSecurityConfig;
import io.crops.warmletter.global.jwt.exception.InvalidRefreshTokenException;
import io.crops.warmletter.global.jwt.provider.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private CorsConfig corsConfig;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("토큰 재발급 - 성공")
    void reissueToken_Success() throws Exception {
        String refreshToken = "valid.refresh.token";
        String newAccessToken = "new.access.token";
        String newRefreshToken = "new.refresh.token";
        TokenResponse tokenResponse = new TokenResponse(newAccessToken, newRefreshToken);

        when(authService.reissue(eq(refreshToken), any(HttpServletResponse.class)))
                .thenReturn(tokenResponse);

        mockMvc.perform(post("/api/auth/reissue")
                        .cookie(new Cookie("refresh_token", refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value(newAccessToken))
                .andExpect(jsonPath("$.data.refreshToken").value(newRefreshToken))
                .andExpect(jsonPath("$.message").value("토큰 재발급 완료"));
    }

    @Test
    @DisplayName("토큰 재발급 - 실패 (리프레시 토큰 없음)")
    void reissueToken_NoRefreshToken() throws Exception {
        when(authService.reissue(isNull(), any(HttpServletResponse.class)))
                .thenThrow(new InvalidRefreshTokenException());

        mockMvc.perform(post("/api/auth/reissue"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH-004"))
                .andExpect(jsonPath("$.message").value("유효하지 않은 리프레시 토큰입니다."))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("토큰 재발급 - 실패 (유효하지 않은 리프레시 토큰)")
    void reissueToken_InvalidRefreshToken() throws Exception {
        String invalidRefreshToken = "invalid.refresh.token";
        when(authService.reissue(eq(invalidRefreshToken), any(HttpServletResponse.class)))
                .thenThrow(new InvalidRefreshTokenException());

        mockMvc.perform(post("/api/auth/reissue")
                        .cookie(new Cookie("refresh_token", invalidRefreshToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH-004"))
                .andExpect(jsonPath("$.message").value("유효하지 않은 리프레시 토큰입니다."))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("로그아웃 - 성공")
    void logout_Success() throws Exception {
        String accessToken = "access.token";
        String refreshToken = "refresh.token";

        mockMvc.perform(post("/api/logout")
                        .header("Authorization", "Bearer " + accessToken)
                        .cookie(new Cookie("refresh_token", refreshToken)))
                .andExpect(status().isOk());

        verify(authService).logout(eq(accessToken), eq(refreshToken), any(HttpServletResponse.class));
    }

    @DisplayName("로그아웃 - 실패 (리프레시 토큰 없음)")
    @Test
    void logout_NoRefreshToken() throws Exception {
        String accessToken = "access.token";

        mockMvc.perform(post("/api/logout")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        verify(authService, never()).logout(any(), any(), any());
    }
}
