package io.crops.warmletter.domain.share.service;

import io.crops.warmletter.domain.auth.facade.AuthFacade;
import io.crops.warmletter.domain.share.cache.PostLikeRedisManager;
import io.crops.warmletter.domain.share.dto.response.SharePostLikeResponse;
import io.crops.warmletter.domain.share.entity.SharePostLike;
import io.crops.warmletter.domain.share.exception.ShareInvalidInputValue;
import io.crops.warmletter.domain.share.repository.SharePostLikeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SharePostLikeServiceTest {

    @Mock
    private PostLikeRedisManager redisManager;

    @Mock
    private SharePostLikeRepository sharePostLikeRepository;

    @Mock
    private AuthFacade authFacade;

    @InjectMocks
    private SharePostLikeService sharePostLikeService;

    @Test
    @DisplayName("좋아요 토글 요청 처리 - Redis에 존재")
    void toggleLike_Redis() {
        // given
        Long postId = 1L;
        Long memberId = 1L;

        when(authFacade.getCurrentUserId()).thenReturn(memberId);
        when(redisManager.getLikedStatus(postId, memberId)).thenReturn(Optional.of(true));

        // when
        sharePostLikeService.toggleLike(postId);

        // then
        verify(authFacade).getCurrentUserId();
        verify(redisManager).getLikedStatus(postId, memberId);
        verify(redisManager).toggleLike(postId, memberId, false); // true -> false로 토글
        verify(sharePostLikeRepository, never()).findBySharePostIdAndMemberId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("좋아요 토글 - Redis에 정보가 없고 DB에 있는 경우")
    void toggleLike_DB() {
        // given
        Long postId = 1L;
        Long memberId = 1L;
        SharePostLike existingLike = mock(SharePostLike.class);

        when(authFacade.getCurrentUserId()).thenReturn(memberId);
        when(redisManager.getLikedStatus(postId, memberId)).thenReturn(Optional.empty());
        when(sharePostLikeRepository.findBySharePostIdAndMemberId(postId, memberId))
                .thenReturn(Optional.of(existingLike));
        when(existingLike.isLiked()).thenReturn(true);

        // when
        sharePostLikeService.toggleLike(postId);

        // then
        verify(authFacade).getCurrentUserId();
        verify(redisManager).getLikedStatus(postId, memberId);
        verify(sharePostLikeRepository).findBySharePostIdAndMemberId(postId, memberId);
        verify(redisManager).toggleLike(postId, memberId, false); // true -> false로 토글
    }

    @Test
    @DisplayName("좋아요 토글 - Redis에 정보가 없고 DB에도 없는 경우")
    void toggleLike_WithoutRedisAndDB() {
        // given
        Long postId = 1L;
        Long memberId = 1L;

        when(authFacade.getCurrentUserId()).thenReturn(memberId);
        when(redisManager.getLikedStatus(postId, memberId)).thenReturn(Optional.empty());
        when(sharePostLikeRepository.findBySharePostIdAndMemberId(postId, memberId))
                .thenReturn(Optional.empty());

        // when
        sharePostLikeService.toggleLike(postId);

        // then
        verify(authFacade).getCurrentUserId();
        verify(redisManager).getLikedStatus(postId, memberId);
        verify(sharePostLikeRepository).findBySharePostIdAndMemberId(postId, memberId);
        verify(redisManager).toggleLike(postId, memberId, true); // false -> true로 토글 (기본값은 false)
    }

    @Test
    @DisplayName("좋아요 개수와 상태 - Redis")
    void getLikeCountAndStatus_WithRedisStatus() {
        // given
        Long sharePostId = 1L;
        Long memberId = 1L;
        SharePostLikeResponse dbResponse = new SharePostLikeResponse(5L, false);

        when(authFacade.getCurrentUserId()).thenReturn(memberId);
        when(sharePostLikeRepository.getLikeCountAndStatus(sharePostId, memberId))
                .thenReturn(dbResponse);
        when(redisManager.getLikeCount(sharePostId)).thenReturn(2);
        when(redisManager.getLikedStatus(sharePostId, memberId)).thenReturn(Optional.of(true));

        // when
        SharePostLikeResponse result = sharePostLikeService.getLikeCountAndStatus(sharePostId);

        // then
        assertNotNull(result);
        assertEquals(7L, result.getLikeCount()); // 5(DB) + 2(Redis) = 7
        assertTrue(result.isLiked()); // Redis 값 사용
        verify(authFacade).getCurrentUserId();
        verify(sharePostLikeRepository).getLikeCountAndStatus(sharePostId, memberId);
        verify(redisManager).getLikeCount(sharePostId);
        verify(redisManager).getLikedStatus(sharePostId, memberId);
    }

    @Test
    @DisplayName("좋아요 개수와 상태 - NoRedis ")
    void getLikeCountAndStatus_SuccessNoRedis() {
        // given
        Long sharePostId = 1L;
        Long memberId = 1L;
        SharePostLikeResponse mockResponse = new SharePostLikeResponse(5L, true);

        when(authFacade.getCurrentUserId()).thenReturn(memberId);
        when(sharePostLikeRepository.getLikeCountAndStatus(sharePostId, memberId))
                .thenReturn(mockResponse);
        when(redisManager.getLikeCount(sharePostId)).thenReturn(0);
        when(redisManager.getLikedStatus(sharePostId, memberId)).thenReturn(Optional.empty());

        // when
        SharePostLikeResponse result = sharePostLikeService.getLikeCountAndStatus(sharePostId);

        // then
        assertNotNull(result);
        assertEquals(5L, result.getLikeCount());
        assertTrue(result.isLiked());
        verify(authFacade).getCurrentUserId();
        verify(sharePostLikeRepository).getLikeCountAndStatus(sharePostId, memberId);
        verify(redisManager).getLikeCount(sharePostId);
        verify(redisManager).getLikedStatus(sharePostId, memberId);
    }

    @Test
    @DisplayName("좋아요 조회 - 좋아요 안 한 경우")
    void getLikeCountAndStatus_NotLiked() {
        // given
        Long sharePostId = 1L;
        Long memberId = 1L;
        SharePostLikeResponse mockResponse = new SharePostLikeResponse(10L, false);

        when(authFacade.getCurrentUserId()).thenReturn(memberId);
        when(sharePostLikeRepository.getLikeCountAndStatus(sharePostId, memberId))
                .thenReturn(mockResponse);

        // when
        SharePostLikeResponse result = sharePostLikeService.getLikeCountAndStatus(sharePostId);

        // then
        assertNotNull(result);
        assertEquals(10L, result.getLikeCount());
        assertFalse(result.isLiked());
        verify(authFacade).getCurrentUserId();
        verify(sharePostLikeRepository).getLikeCountAndStatus(sharePostId, memberId);
    }

    @Test
    @DisplayName("게시물 ID가 null인 경우 예외 발생")
    void getLikeCountAndStatus_NullPostId() {
        // given
        Long memberId = 1L;
        when(authFacade.getCurrentUserId()).thenReturn(memberId);

        // when & then
        assertThrows(ShareInvalidInputValue.class, () -> {
            sharePostLikeService.getLikeCountAndStatus(null);
        });

        verify(authFacade).getCurrentUserId();
        verify(sharePostLikeRepository, never()).getLikeCountAndStatus(any(), any());
        verify(redisManager, never()).getLikeCount(any());
        verify(redisManager, never()).getLikedStatus(any(), any());
    }
}