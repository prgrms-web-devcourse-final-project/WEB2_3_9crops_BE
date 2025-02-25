package io.crops.warmletter.domain.timeline.entity;

import io.crops.warmletter.domain.timeline.enums.AlarmType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

// 임시 테스트
@ActiveProfiles("test")
class TimelineTest {
    @Test
    @DisplayName("타임라인 생성")
    void create_eventComment() {
        // Give
        long memberId = 1;
        String title = "제목";
        String content = "내용";
        AlarmType alarmType = AlarmType.LETTER;

        // When
        Timeline timeLine = Timeline.builder()
                .memberId(memberId)
                .title(title)
                .content(content)
                .alarmType(alarmType)
                .build();

        // Then
        assertNotNull(timeLine);
        assertEquals(memberId, timeLine.getMemberId());
        assertEquals(title, timeLine.getTitle());
        assertEquals(content, timeLine.getContent());
        assertEquals(alarmType, timeLine.getAlarmType());
        assertEquals(false, timeLine.getIsRead());
    }
}