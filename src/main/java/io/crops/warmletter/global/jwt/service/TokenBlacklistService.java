package io.crops.warmletter.global.jwt.service;

import io.crops.warmletter.global.jwt.provider.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtTokenProvider jwtTokenProvider;

    public void blacklistTokens(String accessToken, String refreshToken, String socialUniqueId) {
        // Access 토큰 블랙리스트 처리
        long accessTokenExpirationTime = jwtTokenProvider.getExpirationTime(accessToken);
        redisTemplate.opsForValue().set(
                "blacklist:access_token:" + accessToken,
                "blacklisted",
                accessTokenExpirationTime,
                TimeUnit.MILLISECONDS
        );

        // socialUniqueId가 있으면 이를 사용하여 리프레시 토큰 삭제
        if (StringUtils.hasText(socialUniqueId)) {
            redisTemplate.delete("refresh_token:" + socialUniqueId);
        }
        // 없는 경우 refreshToken에서 추출 시도
        else if (StringUtils.hasText(refreshToken)) {
                String extractedSocialUniqueId = jwtTokenProvider.getSocialUniqueId(refreshToken);
                redisTemplate.delete("refresh_token:" + extractedSocialUniqueId);
        }
    }

    // 블랙리스트에 등록된 Access 토큰인지
    public boolean isAccessTokenBlacklisted(String accessToken) {
        return Boolean.TRUE.equals(
                redisTemplate.hasKey("blacklist:access_token:" + accessToken)
        );
    }
}
