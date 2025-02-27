package io.crops.warmletter.global.jwt.provider;

import io.crops.warmletter.domain.member.enums.Role;
import io.crops.warmletter.global.jwt.enums.TokenType;
import io.crops.warmletter.global.jwt.exception.InvalidTokenException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    private final RedisTemplate<String, String> redisTemplate;

    private final long ACCESS_TOKEN_EXPIRE_TIME = 1000L * 60 * 30; // 30분
    private final long REFRESH_TOKEN_EXPIRE_TIME = 1000L * 60 * 60 * 24 * 14; // 14일

    private Key key; // JWT 서명에 사용할 키

    @PostConstruct
    protected void init() {
        // secretKey를 Base64로 인코딩
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
        // 인코딩된 키로 JWT 서명용 키 생성
        key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    // Access Token 생성
    public String createAccessToken(String socialUniqueId, Role role, String zipCode, Long memberId) {
        Claims claims = Jwts.claims().setSubject(socialUniqueId);
        claims.put("role", role);
        claims.put("zipCode", zipCode);
        claims.put("memberId", memberId);
        Date now = new Date();

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + ACCESS_TOKEN_EXPIRE_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // Refresh Token 생성
    public String createRefreshToken(String socialUniqueId) {
        Claims claims = Jwts.claims().setSubject(socialUniqueId);
        Date now = new Date();

        String refreshToken = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + REFRESH_TOKEN_EXPIRE_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        // Redis에 저장 - key: refresh_token:{email}, value: refreshToken
        redisTemplate.opsForValue().set(
                "refresh_token:" + socialUniqueId,
                refreshToken,
                REFRESH_TOKEN_EXPIRE_TIME,
                TimeUnit.MILLISECONDS
        );

        return refreshToken;
    }

    public String getSocialUniqueId(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.getSubject();
        } catch (ExpiredJwtException e) {
            // 만료된 토큰이어도 socialUniqueId는 추출
            return e.getClaims().getSubject();
        } catch (JwtException e) {
            throw new InvalidTokenException();
        }
    }

    // 토큰의 유효성 검증
    public boolean validateToken(String token, TokenType tokenType) {
        try {
            // 토큰 파싱
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);

            // Redis에서 해당 토큰이 블랙리스트에 있는지 확인
            if (tokenType == TokenType.ACCESS) {
                String isLogout = redisTemplate.opsForValue().get("blacklist:access_token:" + token);
                return isLogout == null;
            } else {
                String storedToken = redisTemplate.opsForValue().get("refresh_token:" + getSocialUniqueId(token));
                return token.equals(storedToken);
            }
        } catch (SecurityException | MalformedJwtException e) {
            throw new JwtException("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            throw new JwtException("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            throw new JwtException("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            throw new JwtException("JWT 토큰이 잘못되었습니다.");
        }
    }

    // 토큰 정보 추출
    public Claims getClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims(); // 만료된 토큰에서도 클레임 정보는 가져올 수 있음
        }
    }

    // Token 남은 유효시간
    public long getExpirationTime(String token) {
        Date expiration = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
        long now = new Date().getTime();
        return (expiration.getTime() - now);
    }
}
