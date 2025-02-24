package io.crops.warmletter.domain.share.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostLikeRedisManagerTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private PostLikeRedisManager postLikeRedisManager;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("좋아요 토글 - 최초 좋아요")
    void toggleLike_Success() {
        // given
        Long postId = 1L;
        Long memberId = 1L;
        String key = "post:1:like:memberId:1";
        when(valueOperations.get(key)).thenReturn(null);

        // when
        postLikeRedisManager.toggleLike(postId, memberId);

        // then
        verify(valueOperations).set(key, "true");
    }

    @Test
    @DisplayName("좋아요 토글 - 좋아요 취소")
    void toggleLike_Cancel() {
        // given
        Long postId = 1L;
        Long memberId = 1L;
        String key = "post:1:like:memberId:1";
        when(valueOperations.get(key)).thenReturn("true");

        // when
        postLikeRedisManager.toggleLike(postId, memberId);

        // then
        verify(valueOperations).set(key, "false");
    }

    @Test
    @DisplayName("모든 좋아요 상태 가져오기")
    void getAllLikeStatus() {
        Set<String> keys = Set.of("post:1:like:memberId:1", "post:2:like:memberId:1");
        when(redisTemplate.keys("post:*:like:memberId:*")).thenReturn(keys);
        when(valueOperations.get("post:1:like:memberId:1")).thenReturn("true");
        when(valueOperations.get("post:2:like:memberId:1")).thenReturn("false");

        Map<String, Boolean> likeStatus = postLikeRedisManager.getAllLikeStatus();

        assertEquals(2, likeStatus.size());
        assertTrue(likeStatus.get("post:1:like:memberId:1"));
        assertFalse(likeStatus.get("post:2:like:memberId:1"));
    }
//        String value = redisTemplate.opsForValue().get(key);
    @Test
    @DisplayName("상태 확인 - 좋아요 없음")
    void isLiked_False() {
        Long postId = 1L;
        Long memberId = 1L;
        String key = "post:1:like:memberId:1";
        when(valueOperations.get(key)).thenReturn("false");

        boolean liked = postLikeRedisManager.isLiked(postId, memberId);
        assertFalse(liked);
        verify(valueOperations).get(key);
    }

    @Test
    @DisplayName("상태확인 - 좋아요 있음 ")
    void isLiked_True() {
        Long postId = 1L;
        Long memberId = 1L;
        String key = "post:1:like:memberId:1";
        when(valueOperations.get(key)).thenReturn("true");

        boolean liked = postLikeRedisManager.isLiked(postId, memberId);
        assertTrue(liked);
        verify(valueOperations).get(key);
    }

    @Test
    @DisplayName("getAllLikeStatus - 숫자가 아닌 값이 저장된 경우 false로 처리")
    void getAllLikeStatus_NonBooleanValue() {
        // given
        Set<String> keys = Set.of("post:1:like:memberId:1");
        when(redisTemplate.keys("post:*:like:memberId:*")).thenReturn(keys);
        when(valueOperations.get("post:1:like:memberId:1")).thenReturn("not-a-boolean");

        // when
        Map<String, Boolean> result = postLikeRedisManager.getAllLikeStatus();

        // then
        assertEquals(1, result.size());
        assertFalse(result.get("post:1:like:memberId:1"));
    }
    @Test
    @DisplayName("isLiked - 값이 'false'인 경우 false 반환")
    void isLiked_WithFalseValue() {
        // given
        Long postId = 1L;
        Long memberId = 1L;
        String key = "post:1:like:memberId:1";
        when(valueOperations.get(key)).thenReturn("false");

        // when
        boolean result = postLikeRedisManager.isLiked(postId, memberId);

        // then
        assertFalse(result);
        verify(valueOperations).get(key);
    }

    @Test
    @DisplayName("좋아요 토글 - false에서 true로 변경")
    void toggleLike_FromFalseToTrue() {
        // given
        Long postId = 1L;
        Long memberId = 1L;
        String key = "post:1:like:memberId:1";
        when(valueOperations.get(key)).thenReturn("false");

        // when
        postLikeRedisManager.toggleLike(postId, memberId);

        // then
        verify(valueOperations).set(key, "true");
    }

    @Test
    @DisplayName("isLiked - 빈 문자열 값일 경우 false 반환")
    void isLiked_WithEmptyString() {
        // given
        Long postId = 1L;
        Long memberId = 1L;
        String key = "post:1:like:memberId:1";
        when(valueOperations.get(key)).thenReturn("");

        // when
        boolean result = postLikeRedisManager.isLiked(postId, memberId);

        // then
        assertFalse(result);
        verify(valueOperations).get(key);
    }

    @Test
    @DisplayName("clearCache - keys가 null인 경우 안전하게 처리")
    void clearCache_WithNullKeys() {
        // given
        when(redisTemplate.keys("post:*:like:memberId:*")).thenReturn(null);

        // when
        postLikeRedisManager.clearCache();

        // then
        verify(redisTemplate, never()).delete(any(Set.class));
    }

    @Test
    @DisplayName("getAllLikeStatus - 일부 값만 null인 혼합 케이스")
    void getAllLikeStatus_MixedNullValues() {
        // given
        Set<String> keys = Set.of("post:1:like:memberId:1", "post:2:like:memberId:1");
        when(redisTemplate.keys("post:*:like:memberId:*")).thenReturn(keys);
        when(valueOperations.get("post:1:like:memberId:1")).thenReturn("true");
        when(valueOperations.get("post:2:like:memberId:1")).thenReturn(null);

        // when
        Map<String, Boolean> result = postLikeRedisManager.getAllLikeStatus();

        // then
        assertEquals(1, result.size());
        assertTrue(result.get("post:1:like:memberId:1"));
        assertNull(result.get("post:2:like:memberId:1"));
    }

    @Test
    @DisplayName("clearCache - 빈 set인 경우 처리")
    void clearCache_WithEmptySet() {
        // given
        when(redisTemplate.keys("post:*:like:memberId:*")).thenReturn(Set.of());

        // when
        postLikeRedisManager.clearCache();

        // then
        verify(redisTemplate, never()).delete(any(Set.class));
    }

}