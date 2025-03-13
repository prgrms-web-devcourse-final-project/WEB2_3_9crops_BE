package io.crops.warmletter.domain.share.cache;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class PostLikeRedisManager {

    private final StringRedisTemplate redisTemplate;
    private static final String POST_LIKE_KEY = "post:%d:like:memberId:%d";
    private static final String POST_LIKE_COUNT_KEY = "post:%d:like:count";

    public void toggleLike(Long postId, Long memberId,boolean status) {

        String key = getKey(postId, memberId);
        redisTemplate.opsForValue().set(key, String.valueOf(status));

        String countKey = getCountKey(postId);
        if (status) {
            redisTemplate.opsForValue().increment(countKey);
        } else {
            redisTemplate.opsForValue().decrement(countKey);
        }
    }

    private String getCountKey(Long postId) {
        return String.format(POST_LIKE_COUNT_KEY, postId);
    }

    public boolean isLiked(Long postId, Long memberId) {
        return getLikedStatus(postId, memberId).orElse(false);
    }

    public Optional<Boolean> getLikedStatus(Long postId, Long memberId) {
        String key = getKey(postId, memberId);
        String value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return Optional.empty(); // 캐시에 해당 정보가 없음
        }

        return Optional.of(Boolean.parseBoolean(value));
    }

    private String getKey(Long postId, Long memberId) {
        return String.format(POST_LIKE_KEY, postId, memberId);
    }

    public Map<String, Boolean> getAllLikeStatus() {
        Set<String> keys = redisTemplate.keys("post:*:like:memberId:*");
        Map<String, Boolean> likeStatusMap = new HashMap<>();
        if (keys != null) {
            for (String key : keys) {
                String value = redisTemplate.opsForValue().get(key);
                if (value != null) {
                    likeStatusMap.put(key, Boolean.parseBoolean(value));
                }
            }
        }
        return likeStatusMap;
    }

    public int getLikeCount(Long postId) {
        String countKey = getCountKey(postId);
        String countValue = redisTemplate.opsForValue().get(countKey);
        return countValue != null ? Integer.parseInt(countValue) : 0;
    }

    public void clearCache() {
        Set<String> keys = redisTemplate.keys("post:*:like:memberId:*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
        // 좋아요 카운트도 함께 삭제
        Set<String> countKeys = redisTemplate.keys("post:*:like:count");
        if (countKeys != null && !countKeys.isEmpty()) {
            redisTemplate.delete(countKeys);
        }
    }
}
