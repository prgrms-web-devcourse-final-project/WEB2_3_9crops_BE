package io.crops.warmletter.domain.auth.controller;

import io.crops.warmletter.domain.auth.dto.TokenResponse;
import io.crops.warmletter.domain.auth.dto.TokenStorageResponse;
import io.crops.warmletter.domain.auth.service.AuthService;
import io.crops.warmletter.global.config.CorsConfig;
import io.crops.warmletter.global.config.TestSecurityConfig;
import io.crops.warmletter.global.jwt.exception.InvalidRefreshTokenException;
import io.crops.warmletter.global.jwt.exception.InvalidTokenException;
import io.crops.warmletter.global.jwt.provider.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
        String bearerToken = "Bearer valid.access.token";  // Bearer 포함
        String accessToken = "valid.access.token";
        String refreshToken = "valid.refresh.token";
        String newAccessToken = "new.access.token";
        String newRefreshToken = "new.refresh.token";
        TokenResponse tokenResponse = new TokenResponse(newAccessToken, newRefreshToken);

        when(authService.reissue(eq(accessToken), eq(refreshToken), any(HttpServletResponse.class)))
                .thenReturn(tokenResponse);

        mockMvc.perform(post("/api/reissue")
                        .header("Authorization", bearerToken)
                        .cookie(new Cookie("refresh_token", refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value(newAccessToken))
                .andExpect(jsonPath("$.data.refreshToken").value(newRefreshToken))
                .andExpect(jsonPath("$.message").value("토큰 재발급 완료"));
    }

    @Test
    @DisplayName("토큰 재발급 - 실패 (리프레시 토큰 없음)")
    void reissueToken_NoRefreshToken() throws Exception {
        String bearerToken = "Bearer valid.access.token";

        mockMvc.perform(post("/api/reissue")
                        .header("Authorization", bearerToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH-006"))
                .andExpect(jsonPath("$.message").value("유효하지 않은 토큰입니다."))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("토큰 재발급 - 실패 (유효하지 않은 리프레시 토큰)")
    void reissueToken_InvalidRefreshToken() throws Exception {
        String bearerToken = "Bearer valid.access.token";
        String accessToken = "valid.access.token";  // Bearer 제거된 버전
        String invalidRefreshToken = "invalid.refresh.token";

        // refreshToken, accessToken 순서로 파라미터 전달
        when(authService.reissue(eq(accessToken), eq(invalidRefreshToken), any(HttpServletResponse.class)))
                .thenThrow(new InvalidRefreshTokenException());

        mockMvc.perform(post("/api/reissue")
                        .header("Authorization", bearerToken)
                        .cookie(new Cookie("refresh_token", invalidRefreshToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH-004"))
                .andExpect(jsonPath("$.message").value("유효하지 않은 리프레시 토큰입니다."))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("토큰 재발급 - 실패 (잘못된 Bearer 토큰 형식)")
    void reissueToken_InvalidBearerFormat() throws Exception {
        String invalidBearerToken = "Invalid valid.access.token";  // "Bearer " 대신 "Invalid "
        String refreshToken = "valid.refresh.token";

        mockMvc.perform(post("/api/reissue")
                        .header("Authorization", invalidBearerToken)
                        .cookie(new Cookie("refresh_token", refreshToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH-005"))
                .andExpect(jsonPath("$.message").value("유효하지 않은 엑세스 토큰입니다."))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("토큰 재발급 - 실패 (Bearer 접두어 없음)")
    void reissueToken_NoBearerPrefix() throws Exception {
        String tokenWithoutBearer = "valid.access.token";  // Bearer 접두어 없음
        String refreshToken = "valid.refresh.token";

        mockMvc.perform(post("/api/reissue")
                        .header("Authorization", tokenWithoutBearer)
                        .cookie(new Cookie("refresh_token", refreshToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH-005"))
                .andExpect(jsonPath("$.message").value("유효하지 않은 엑세스 토큰입니다."))
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
    
    @DisplayName("임시 토큰 저장소 조회 API 호출 실패 - 조회된 토큰 없음")
    @Test
    void getToken_Fail_NotFoundTemporaryToken() throws Exception {
        //given
        String stateToken = "state.token";

        when(authService.getTokenByState(stateToken)).thenThrow(new InvalidTokenException());
        //when & then
        mockMvc.perform(get("/api/auth/token")
                        .param("state",stateToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH-006"))
                .andExpect(jsonPath("$.message").value("유효하지 않은 토큰입니다."))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(authService).getTokenByState(stateToken);
    }

    @DisplayName("임시 토큰 저장소 조회 API 호출 성공")
    @Test
    void getToken_Success() throws Exception {
        // given
        String stateToken = "test.state.token";
        TokenStorageResponse tokenResponse = TokenStorageResponse.builder()
                .accessToken("access.token")
                .hasZipCode(true)
                .userId(1L)
                .build();

        when(authService.getTokenByState(stateToken)).thenReturn(tokenResponse);

        // when & then
        mockMvc.perform(get("/api/auth/token")
                        .param("state", stateToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("access.token"))
                .andExpect(jsonPath("$.data.hasZipCode").value(true))
                .andExpect(jsonPath("$.data.userId").value(1L))
                .andExpect(jsonPath("$.message").value("임시 저장소 토큰 조회 완료"));

        verify(authService).getTokenByState(stateToken);
    }
}
