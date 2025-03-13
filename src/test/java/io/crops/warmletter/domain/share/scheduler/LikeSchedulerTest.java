package io.crops.warmletter.domain.share.scheduler;

import io.crops.warmletter.domain.share.entity.SharePostLike;
import io.crops.warmletter.domain.share.cache.PostLikeRedisManager;
import io.crops.warmletter.domain.share.repository.SharePostLikeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LikeSchedulerTest {

    @Mock
    private PostLikeRedisManager postLikeRedisManager;

    @Mock
    private SharePostLikeRepository sharePostLikeRepository;

    @InjectMocks
    private LikeScheduler likeScheduler;

    @Test
    @DisplayName("Redis 데이터를 DB에 동기화 - 기존 데이터 ")
    void syncLikesToDatabase() {
        // given
        Map<String, Boolean> likeStatusMap = new HashMap<>();
        likeStatusMap.put("post:1:like:memberId:1", true);

        SharePostLike existingLike = mock(SharePostLike.class);
        when(existingLike.isLiked()).thenReturn(false);

        when(postLikeRedisManager.getAllLikeStatus()).thenReturn(likeStatusMap);
        when(sharePostLikeRepository.findBySharePostIdAndMemberId(1L, 1L))
                .thenReturn(Optional.of(existingLike));

        // when
        likeScheduler.syncLikesToDatabase();

        // then
        verify(existingLike).updateLikeStatus(true);
        verify(postLikeRedisManager).clearCache();
    }

    @Test
    @DisplayName("Redis 데이터를 DB에 동기화 - 새로운 데이터 저장")
    void syncLikesToDatabase_CreateNew() {
        Map<String, Boolean> likeStatusMap = new HashMap<>();
        likeStatusMap.put("post:2:like:memberId:1", true);

        when(postLikeRedisManager.getAllLikeStatus()).thenReturn(likeStatusMap);
        when(sharePostLikeRepository.findBySharePostIdAndMemberId(2L, 1L))
                .thenReturn(Optional.empty());

        likeScheduler.syncLikesToDatabase();

        verify(sharePostLikeRepository).save(any(SharePostLike.class));
        verify(postLikeRedisManager).clearCache();
    }

    @Test
    @DisplayName("Redis 데이터를 DB에 동기화 - 상태가 같은 경우 업데이트 안함 ")
    void syncLikesToDatabase_Already() {
        Map<String, Boolean> likeStatusMap = new HashMap<>();
        likeStatusMap.put("post:1:like:memberId:1", true);

        SharePostLike existingLike = mock(SharePostLike.class);
        when(existingLike.isLiked()).thenReturn(true); // 이미 같은 상태

        when(postLikeRedisManager.getAllLikeStatus()).thenReturn(likeStatusMap);
        when(sharePostLikeRepository.findBySharePostIdAndMemberId(1L, 1L))
                .thenReturn(Optional.of(existingLike));

        likeScheduler.syncLikesToDatabase();

        verify(existingLike, never()).updateLikeStatus(anyBoolean()); // 상태가 같으므로 업데이트하지 않음
        verify(postLikeRedisManager).clearCache();
    }

    @Test
    @DisplayName("Redis 데이터를 DB에 동기화 - 좋아요 상태가 false인 경우 저장하지 않음")
    void syncLikesToDatabase_IgnoreFalseStatus() {
        // given
        Map<String, Boolean> likeStatusMap = new HashMap<>();
        likeStatusMap.put("post:2:like:memberId:1", false);

        when(postLikeRedisManager.getAllLikeStatus()).thenReturn(likeStatusMap);
        when(sharePostLikeRepository.findBySharePostIdAndMemberId(2L, 1L))
                .thenReturn(Optional.empty());

        // when
        likeScheduler.syncLikesToDatabase();

        // then
        verify(sharePostLikeRepository, never()).save(any(SharePostLike.class));
        verify(postLikeRedisManager).clearCache();
    }
    @Test
    @DisplayName("Redis 데이터를 DB에 동기화 - 빈 상태맵 처리")
    void syncLikesToDatabase_EmptyStatusMap() {
        // given
        Map<String, Boolean> emptyMap = new HashMap<>();
        when(postLikeRedisManager.getAllLikeStatus()).thenReturn(emptyMap);

        // when
        likeScheduler.syncLikesToDatabase();

        // then
        verify(postLikeRedisManager).clearCache();
        verify(sharePostLikeRepository, never()).findBySharePostIdAndMemberId(any(), any());
        verify(sharePostLikeRepository, never()).save(any());
    }

    @Test
    @DisplayName("Redis 데이터를 DB에 동기화 - 처리 중 예외 발생")
    void syncLikesToDatabase_ExceptionHandling() {
        // given
        Map<String, Boolean> likeStatusMap = new HashMap<>();
        likeStatusMap.put("post:1:like:memberId:1", true);
        likeStatusMap.put("post:2:like:memberId:1", true);

        when(postLikeRedisManager.getAllLikeStatus()).thenReturn(likeStatusMap);
        when(sharePostLikeRepository.findBySharePostIdAndMemberId(1L, 1L))
                .thenThrow(new RuntimeException("Database error"));
        when(sharePostLikeRepository.findBySharePostIdAndMemberId(2L, 1L))
                .thenReturn(Optional.empty());

        // when
        likeScheduler.syncLikesToDatabase();

        // then
        // 첫 번째 항목 처리 중 예외가 발생해도 두 번째 항목은 처리되어야 함
        verify(sharePostLikeRepository).save(any(SharePostLike.class));
        verify(postLikeRedisManager).clearCache();
    }
    @Test
    @DisplayName("Redis 데이터를 DB에 동기화 - 잘못된 키 형식 처리")
    void syncLikesToDatabase_InvalidKeyFormat() {
        // given
        Map<String, Boolean> likeStatusMap = new HashMap<>();
        likeStatusMap.put("post:1:like:memberId:1", true); // 유효한 키
        likeStatusMap.put("invalid:format:key", true); // 잘못된 키

        when(postLikeRedisManager.getAllLikeStatus()).thenReturn(likeStatusMap);
        when(sharePostLikeRepository.findBySharePostIdAndMemberId(1L, 1L))
                .thenReturn(Optional.empty());

        // when
        likeScheduler.syncLikesToDatabase();

        // then
        // 유효한 키만 처리되어야 함
        verify(sharePostLikeRepository, times(1)).findBySharePostIdAndMemberId(anyLong(), anyLong());
        verify(sharePostLikeRepository).save(any(SharePostLike.class));
        verify(postLikeRedisManager).clearCache();
    }

    @Test
    @DisplayName("Redis 데이터 조회 중 예외 발생")
    void syncLikesToDatabase_RedisException() {
        // given
        when(postLikeRedisManager.getAllLikeStatus()).thenThrow(new RuntimeException("Redis error"));

        // when
        likeScheduler.syncLikesToDatabase();

        // then
        // 예외가 발생해도 테스트는 통과해야 함 (예외가 잡혀야 함)
        verify(postLikeRedisManager, never()).clearCache();
    }
    @Test
    @DisplayName("Redis getAllLikeStatus가 null 반환")
    void syncLikesToDatabase_NullStatusMap() {
        // given
        when(postLikeRedisManager.getAllLikeStatus()).thenReturn(null);

        // when
        likeScheduler.syncLikesToDatabase();

        // then
        verify(postLikeRedisManager).clearCache();
        verify(sharePostLikeRepository, never()).findBySharePostIdAndMemberId(any(), any());
    }

}
