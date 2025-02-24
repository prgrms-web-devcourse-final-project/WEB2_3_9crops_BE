package io.crops.warmletter.domain.share.scheduler;

import io.crops.warmletter.domain.share.entity.SharePostLike;
import io.crops.warmletter.domain.share.redis.PostLikeRedisManager;
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
    @DisplayName("Redis 데이터를 DB에 동기화")
    void syncLikesToDatabase() {
        // given
        Map<String, Boolean> likeStatusMap = new HashMap<>();
        likeStatusMap.put("post:1:like:memberId:1", true);

        SharePostLike existingLike = new SharePostLike(1L, 1L, false);

        when(postLikeRedisManager.getAllLikeStatus()).thenReturn(likeStatusMap);
        when(sharePostLikeRepository.findBySharePostIdAndMemberId(1L, 1L))
                .thenReturn(Optional.of(existingLike));

        // when
        likeScheduler.syncLikesToDatabase();

        // then
        verify(postLikeRedisManager).clearCache();
        assertThat(existingLike.isLiked()).isTrue();
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
    @DisplayName("Redis 데이터를 DB에 동기화 - 기존 데이터 존재시 반대")
    void syncLikesToDatabase_Already() {
        Map<String, Boolean> likeStatusMap = new HashMap<>();
        likeStatusMap.put("post:1:like:memberId:1", true);

        SharePostLike sharePostLike = new SharePostLike(1L, 1L, true);

        when(postLikeRedisManager.getAllLikeStatus()).thenReturn(likeStatusMap);
        when(sharePostLikeRepository.findBySharePostIdAndMemberId(1L, 1L))
                .thenReturn(Optional.of(sharePostLike));

        likeScheduler.syncLikesToDatabase();

        verify(postLikeRedisManager).clearCache();
        assertThat(sharePostLike.isLiked()).isFalse();

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

}
