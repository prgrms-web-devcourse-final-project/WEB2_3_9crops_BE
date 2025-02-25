package io.crops.warmletter.domain.share.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SharePostLikeTest {

    @Test
    @DisplayName("엔티티 생성 테스트")
    void createEntity_Success() {
        // given
        Long postId = 1L;
        Long memberId = 1L;
        boolean isLiked = true;

        // when
        SharePostLike sharePostLike = SharePostLike.builder()
                .sharePostId(postId)
                .memberId(memberId)
                .isLiked(isLiked)
                .build();

        // then
        assertThat(sharePostLike.getSharePostId()).isEqualTo(postId);
        assertThat(sharePostLike.getMemberId()).isEqualTo(memberId);
        assertThat(sharePostLike.isLiked()).isEqualTo(isLiked);
    }

    @Test
    @DisplayName("좋아요 상태 업데이트 테스트")
    void updateLikeStatus_Success() {
        // given
        SharePostLike sharePostLike = SharePostLike.builder()
                .sharePostId(1L)
                .memberId(1L)
                .isLiked(false)
                .build();

        // when
        sharePostLike.updateLikeStatus(true);

        // then
        assertThat(sharePostLike.isLiked()).isTrue();
    }
}
