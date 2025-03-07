package io.crops.warmletter.domain.auth.service;

import io.crops.warmletter.domain.auth.dto.TokenResponse;
import io.crops.warmletter.domain.auth.dto.TokenStorageResponse;
import io.crops.warmletter.domain.auth.exception.UnauthorizedException;
import io.crops.warmletter.domain.member.entity.Member;
import io.crops.warmletter.domain.member.enums.Role;
import io.crops.warmletter.domain.member.exception.MemberNotFoundException;
import io.crops.warmletter.domain.member.repository.MemberRepository;
import io.crops.warmletter.global.jwt.enums.TokenType;
import io.crops.warmletter.global.jwt.exception.InvalidRefreshTokenException;
import io.crops.warmletter.global.jwt.exception.InvalidTokenException;
import io.crops.warmletter.global.jwt.provider.JwtTokenProvider;
import io.crops.warmletter.global.jwt.service.TokenBlacklistService;
import io.crops.warmletter.global.jwt.service.TokenStorage;
import io.crops.warmletter.global.oauth.entity.UserPrincipal;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthService {

    @Value("${server.domain}")
    private String serverDomain;

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;
    private final MemberRepository memberRepository;
    private final TokenStorage tokenStorage;
    private final long REFRESH_TOKEN_EXPIRE_TIME = 1000L * 60 * 60 * 24 * 14; // 14일
    private final long REFRESH_TOKEN_REISSUE_TIME = REFRESH_TOKEN_EXPIRE_TIME / 2; // 7일

    public TokenResponse reissue(String refreshToken, HttpServletResponse response) {
        // 리프레시 토큰 검증
        if (!jwtTokenProvider.validateToken(refreshToken, TokenType.REFRESH)) {
            throw new InvalidRefreshTokenException();
        }

        String socialUniqueId = jwtTokenProvider.getSocialUniqueId(refreshToken);
        // 사용자 정보 DB에서 조회
        Member member = memberRepository.findBySocialUniqueId(socialUniqueId)
                .orElseThrow(MemberNotFoundException::new);

        // 새 액세스 토큰 생성
        String newAccessToken = jwtTokenProvider.createAccessToken(
                socialUniqueId,
                member.getRole(),
                member.getZipCode(),
                member.getId()
        );

        // Access Token을 Authorization 헤더에 추가
        response.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + newAccessToken);

        // 리프레시 토큰 만료가 임박한 경우 재발급
        if (jwtTokenProvider.getExpirationTime(refreshToken) < REFRESH_TOKEN_REISSUE_TIME) {
            String newRefreshToken = jwtTokenProvider.createRefreshToken(socialUniqueId);

            // Refresh Token을 쿠키에 저장
            ResponseCookie refreshTokenCookie = ResponseCookie.from("refresh_token", newRefreshToken)
                    // 배포용
                    .httpOnly(true)    // JavaScript에서 쿠키에 접근할 수 없도록 설정
                    .secure(true)      // HTTPS에서만 쿠키가 전송되도록 설정
                    .sameSite("None")  // 크로스 도메인 요청 허용
                    .domain(serverDomain)
                    .path("/")         // 쿠키가 유효한 경로 설정
                    .maxAge(Duration.ofDays(14))  // 쿠키의 유효기간 설정 (14일)
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

            return new TokenResponse(newAccessToken, newRefreshToken);
        }

        return new TokenResponse(newAccessToken, refreshToken);
    }

    public void logout(String accessToken, String refreshToken, HttpServletResponse response) {
        String socialUniqueId = getCurrentUser().getSocialUniqueId();

        // 토큰 무효화
        tokenBlacklistService.blacklistTokens(accessToken, refreshToken, socialUniqueId);

        // 리프레시 토큰 쿠키 제거
        Cookie cookie = new Cookie("refresh_token", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        response.addCookie(cookie);
    }

    public UserPrincipal getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException();
        }
        return (UserPrincipal) authentication.getPrincipal();
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public String getZipCode() {
        return getCurrentUser().getZipCode();
    }

    public TokenStorageResponse getTokenByState(String stateToken) {
        TokenStorage.TokenInfo tokenInfo = tokenStorage.getTokenInfo(stateToken);

        if (tokenInfo == null) {
            throw new InvalidTokenException();
        }

        return TokenStorageResponse.builder()
                .accessToken(tokenInfo.getAccessToken())
                .build();
    }
}
