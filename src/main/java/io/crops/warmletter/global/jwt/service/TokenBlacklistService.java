package io.crops.warmletter.global.jwt.service;

import io.crops.warmletter.global.jwt.provider.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtTokenProvider jwtTokenProvider;

    public void blacklistTokens(String accessToken, String refreshToken) {
        // Access 토큰 블랙리스트 처리
        long accessTokenExpirationTime = jwtTokenProvider.getExpirationTime(accessToken);
        redisTemplate.opsForValue().set(
                "blacklist:access_token:" + accessToken,
                "blacklisted",
                accessTokenExpirationTime,
                TimeUnit.MILLISECONDS
        );

        // Refresh 토큰 삭제
        redisTemplate.delete("refresh_token:" + jwtTokenProvider.getSocialUniqueId(refreshToken));
    }

    // 블랙리스트에 등록된 Access 토큰인지
    public boolean isAccessTokenBlacklisted(String accessToken) {
        return Boolean.TRUE.equals(
                redisTemplate.hasKey("blacklist:access_token:" + accessToken)
        );
    }
}
