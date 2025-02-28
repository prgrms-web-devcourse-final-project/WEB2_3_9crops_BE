package io.crops.warmletter.domain.share.service;

import io.crops.warmletter.domain.auth.facade.AuthFacade;
import io.crops.warmletter.domain.share.cache.PostLikeRedisManager;
import io.crops.warmletter.domain.share.dto.response.SharePostLikeResponse;
import io.crops.warmletter.domain.share.exception.ShareInvalidInputValue;
import io.crops.warmletter.domain.share.repository.SharePostLikeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    @DisplayName("좋아요 토글 요청 처리")
    void toggleLike() {
        // given
        Long postId = 1L;
        Long memberId = 1L;

        when(authFacade.getCurrentUserId()).thenReturn(memberId);

        // when
        sharePostLikeService.toggleLike(postId);

        // then
        verify(authFacade).getCurrentUserId();
        verify(redisManager).toggleLike(postId, memberId);
    }

    @Test
    @DisplayName("좋아요 개수와 상태 조회 성공")
    void getLikeCountAndStatus_Success() {
        // given
        Long sharePostId = 1L;
        Long memberId = 1L;
        SharePostLikeResponse mockResponse = new SharePostLikeResponse(5L, true);

        when(authFacade.getCurrentUserId()).thenReturn(memberId);
        when(sharePostLikeRepository.getLikeCountAndStatus(sharePostId, memberId))
                .thenReturn(mockResponse);

        // when
        SharePostLikeResponse result = sharePostLikeService.getLikeCountAndStatus(sharePostId);

        // then
        assertNotNull(result);
        assertEquals(5L, result.getLikeCount());
        assertTrue(result.isLiked());
        verify(authFacade).getCurrentUserId();
        verify(sharePostLikeRepository).getLikeCountAndStatus(sharePostId, memberId);
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
    }
}