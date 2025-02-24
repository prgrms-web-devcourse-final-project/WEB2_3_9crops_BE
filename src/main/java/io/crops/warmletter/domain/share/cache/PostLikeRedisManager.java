package io.crops.warmletter.domain.share.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class PostLikeRedisManager {

    private final StringRedisTemplate redisTemplate;
    private static final String POST_LIKE_KEY = "post:%d:like:memberId:%d";

    public void toggleLike(Long postId, Long memberId) {
        String key = getKey(postId, memberId);
        Boolean isLiked = isLiked(postId, memberId);
        redisTemplate.opsForValue().set(key, String.valueOf(!isLiked));
    }

    public boolean isLiked(Long postId, Long memberId) {
        String key = getKey(postId, memberId);
        String value = redisTemplate.opsForValue().get(key);
        return value != null && Boolean.parseBoolean(value);
    }

    private String getKey(Long postId, Long memberId) {
        return String.format(POST_LIKE_KEY, postId, memberId);
    }

    public Map<String, Boolean> getAllLikeStatus() {
        Set<String> keys = redisTemplate.keys("post:*:like:memberId:*");
        Map<String, Boolean> likeStatusMap = new HashMap<>();

        for (String key : keys) {
            String value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                likeStatusMap.put(key, Boolean.parseBoolean(value));
            }
        }
        return likeStatusMap;
    }


    public void clearCache() {
        Set<String> keys = redisTemplate.keys("post:*:like:memberId:*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

}
