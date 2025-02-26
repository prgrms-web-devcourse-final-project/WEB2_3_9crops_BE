package io.crops.warmletter.domain.share.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ShareLetterPostResponseTest {

    @Test
    @DisplayName("Builder를 사용하여 객체가 올바르게 생성되는지 검증")
    void createResponseWithBuilder() {
        // given
        Long id = 1L;
        String content = "편지 내용";
        String writerZipCode = "12345";
        String receiverZipCode = "67890";
        LocalDateTime createdAt = LocalDateTime.now();

        // when
        ShareLetterPostResponse response = ShareLetterPostResponse.builder()
                .id(id)
                .content(content)
                .writerZipCode(writerZipCode)
                .receiverZipCode(receiverZipCode)
                .createdAt(createdAt)
                .build();

        // then
        assertAll(
                () -> assertThat(response.getId()).isEqualTo(id),
                () -> assertThat(response.getContent()).isEqualTo(content),
                () -> assertThat(response.getWriterZipCode()).isEqualTo(writerZipCode),
                () -> assertThat(response.getReceiverZipCode()).isEqualTo(receiverZipCode),
                () -> assertThat(response.getCreatedAt()).isEqualTo(createdAt)
        );
    }
    @Test
    @DisplayName("SharePostLikeResponse 생성 및 getter 테스트")
    void testSharePostLikeResponse() {
        // given
        Long likeCount = 42L;
        boolean isLiked = true;

        // when
        SharePostLikeResponse response = new SharePostLikeResponse(likeCount, isLiked);

        // then
        assertEquals(likeCount, response.getLikeCount());
        assertEquals(isLiked, response.isLiked());
    }

    @Test
    @DisplayName("좋아요 안 한 상태의 SharePostLikeResponse 테스트")
    void testSharePostLikeResponseNotLiked() {
        // given
        Long likeCount = 10L;
        boolean isLiked = false;

        // when
        SharePostLikeResponse response = new SharePostLikeResponse(likeCount, isLiked);

        // then
        assertEquals(likeCount, response.getLikeCount());
        assertFalse(response.isLiked());
    }
}