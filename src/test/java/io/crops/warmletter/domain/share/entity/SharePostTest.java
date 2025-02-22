package io.crops.warmletter.domain.share.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class SharePostTest {

    @Test
    @DisplayName("SharePost 생성 성공")
    void createSharePost() {
        // given
        Long shareProposalId = 1L;
        String content = "내용";
        boolean isActive = true;
        // when
        SharePost sharePost = SharePost.builder()
                .shareProposalId(shareProposalId)
                .content(content)
                .isActive(isActive)
                .build();

        // then
        assertAll(
                () -> assertThat(sharePost.getShareProposalId()).isEqualTo(shareProposalId),
                () -> assertThat(sharePost.getContent()).isEqualTo(content),
                () -> assertThat(sharePost.isActive()).isTrue()
        );
    }

    @Test
    @DisplayName("게시글 활성화 성공")
    void activateSharePost() {
        // given
        SharePost sharePost = SharePost.builder()
                .shareProposalId(1L)
                .content("내용")
                .isActive(true)
                .build();

        // when
        sharePost.activate();

        // then
        assertThat(sharePost.isActive()).isTrue();
    }
}