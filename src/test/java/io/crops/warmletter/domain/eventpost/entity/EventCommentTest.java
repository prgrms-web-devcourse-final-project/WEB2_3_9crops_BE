package io.crops.warmletter.domain.eventpost.entity;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

// 임시 테스트
@ActiveProfiles("test")
class EventCommentTest {

    @Test
    @DisplayName("이벤트 게시판 댓글 값 생성")
    void create_eventComment() {
        // Given
        long eventPostId = 1L;
        long writerId = 2L;
        String content = "내용";

        // When
        EventComment eventComment = EventComment.builder()
                .eventPostId(eventPostId)
                .writerId(writerId)
                .content(content)
                .build();

        // Then
        assertNotNull(eventComment);
        assertEquals(eventPostId, eventComment.getEventPostId());
        assertEquals(writerId, eventComment.getWriterId());
        assertEquals(content, eventComment.getContent());
        assertTrue(eventComment.isActive());
    }

    @Test
    @DisplayName("이벤트 게시판 댓글 소프트 딜리트")
    void update_IsActive_ToFalse() {
        // Given
        EventComment eventComment = EventComment.builder()
                .eventPostId(1L)
                .writerId(2L)
                .content("내용")
                .build();

        // When
        eventComment.softDelete();

        // Then
        assertFalse(eventComment.isActive());
    }
}
