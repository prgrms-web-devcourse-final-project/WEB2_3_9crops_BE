package io.crops.warmletter.global.jwt.filter;

import io.crops.warmletter.domain.member.enums.Role;
import io.crops.warmletter.global.jwt.enums.TokenType;
import io.crops.warmletter.global.jwt.provider.JwtTokenProvider;
import io.crops.warmletter.global.oauth.entity.UserPrincipal;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 헤더에서 JWT 토큰 추출
        String token = resolveToken(request);

        // 토큰이 유효하면 인증 정보 설정
        if (token != null && jwtTokenProvider.validateToken(token, TokenType.ACCESS)) {
            Claims claims = jwtTokenProvider.getClaims(token);

            UserPrincipal userPrincipal = UserPrincipal.builder()
                    .id(claims.get("memberId", Long.class))
                    .socialUniqueId(claims.getSubject())  // socialUniqueId는 subject에서
                    .role(Role.valueOf(claims.get("role", String.class)))
                    .zipCode(claims.get("zipCode", String.class))
                    .authorities(Collections.singletonList(
                            new SimpleGrantedAuthority("ROLE_" + claims.get("role", String.class))
                    ))
                    .build();

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userPrincipal,
                    null,
                    userPrincipal.getAuthorities()
            );

            // SecurityContext에 인증 정보 저장
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    // 헤더에서 토큰 추출
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
