package io.crops.warmletter.global.jwt.service;

import io.crops.warmletter.global.oauth.entity.UserPrincipal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenStorage {
    private final Map<String, TokenInfo> tokenStore = new ConcurrentHashMap<>();
    private final long EXPIRY_TIME = 1000L * 60 * 3; // 3분

    @Scheduled(fixedRate = 300000) // 5분마다 만료된 토큰 정리
    public void cleanupExpiredTokens() {
        long now = System.currentTimeMillis();
        tokenStore.entrySet().removeIf(entry -> now - entry.getValue().getCreatedAt() > EXPIRY_TIME);
    }

    public void storeTemporaryToken(String stateToken, String accessToken, UserPrincipal userInfo) {
        tokenStore.put(stateToken, new TokenInfo(accessToken, userInfo, System.currentTimeMillis()));
    }

    public TokenInfo getTokenInfo(String stateToken) {
        TokenInfo tokenInfo = tokenStore.get(stateToken);
        if (tokenInfo != null) {
            // 한 번 사용한 토큰은 제거 (일회용)
            tokenStore.remove(stateToken);
            return tokenInfo;
        }
        return null;
    }

    @Getter
    @AllArgsConstructor
    public static class TokenInfo {
        private String accessToken;
        private UserPrincipal userInfo;
        private long createdAt;
    }
}
