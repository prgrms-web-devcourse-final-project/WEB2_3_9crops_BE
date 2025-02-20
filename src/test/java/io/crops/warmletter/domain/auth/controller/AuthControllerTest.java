package io.crops.warmletter.domain.auth.controller;

import io.crops.warmletter.domain.auth.dto.TokenResponse;
import io.crops.warmletter.domain.auth.service.AuthService;
import io.crops.warmletter.global.config.CorsConfig;
import io.crops.warmletter.global.config.TestSecurityConfig;
import io.crops.warmletter.global.jwt.exception.InvalidRefreshTokenException;
import io.crops.warmletter.global.jwt.provider.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
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
    private CorsConfig corsConfig;  // 추가

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;  // 추가

    @DisplayName("토큰 재발급 - 성공")
    @Test
    void reissueToken_Success() throws Exception {
        // given
        String refreshToken = "valid.refresh.token";
        String newAccessToken = "new.access.token";
        String newRefreshToken = "new.refresh.token";

        TokenResponse tokenResponse = new TokenResponse(newAccessToken, newRefreshToken);
        when(authService.reissue(refreshToken)).thenReturn(tokenResponse);

        // when & then
        mockMvc.perform(post("/api/auth/reissue")
                        .with(csrf())
                        .cookie(new Cookie("refresh_token", refreshToken)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.AUTHORIZATION, "Bearer " + newAccessToken))
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andExpect(jsonPath("$.data.accessToken").value(newAccessToken))
                .andExpect(jsonPath("$.data.refreshToken").value(newRefreshToken))
                .andExpect(jsonPath("$.message").value("토큰 재발급 완료"));
    }

    @DisplayName("토큰 재발급 - 실패 (리프레시 토큰 없음)")
    @Test
    void reissueToken_NoRefreshToken() throws Exception {
        // when & then
        when(authService.reissue(null))
                .thenThrow(new InvalidRefreshTokenException());

        mockMvc.perform(post("/api/auth/reissue"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH-004"))
                .andExpect(jsonPath("$.message").value("유효하지 않은 리프레시 토큰입니다."))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @DisplayName("토큰 재발급 - 실패 (유효하지 않은 리프레시 토큰)")
    @Test
    void reissueToken_InvalidRefreshToken() throws Exception {
        // given
        String invalidRefreshToken = "invalid.refresh.token";
        when(authService.reissue(invalidRefreshToken))
                .thenThrow(new InvalidRefreshTokenException());

        // when & then
        mockMvc.perform(post("/api/auth/reissue")
                        .with(csrf())
                        .cookie(new Cookie("refresh_token", invalidRefreshToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH-004"))
                .andExpect(jsonPath("$.message").value("유효하지 않은 리프레시 토큰입니다."))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
