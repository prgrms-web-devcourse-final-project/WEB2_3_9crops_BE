package io.crops.warmletter.domain.share.service;

import io.crops.warmletter.domain.share.cache.PostLikeRedisManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SharePostLikeServiceTest {

    @Mock
    private PostLikeRedisManager redisManager;
    @InjectMocks
    private SharePostLikeService sharePostLikeService;

    @Test
    @DisplayName("좋아요 토글 요청 처리")
    void toggleLike() {
        // given
        Long postId = 1L;
        Long memberId = 1L;

        // when
        sharePostLikeService.toggleLike(postId, memberId);

        // then
        verify(redisManager).toggleLike(postId, memberId);
    }


}