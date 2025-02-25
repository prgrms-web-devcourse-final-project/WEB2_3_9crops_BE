package io.crops.warmletter.domain.timeline.service;

import io.crops.warmletter.domain.eventpost.dto.response.EventCommentsResponse;
import io.crops.warmletter.domain.eventpost.dto.response.EventPostDetailResponse;
import io.crops.warmletter.domain.eventpost.entity.EventPost;
import io.crops.warmletter.domain.eventpost.repository.EventCommentRepository;
import io.crops.warmletter.domain.eventpost.repository.EventPostRepository;
import io.crops.warmletter.domain.eventpost.service.EventPostService;
import io.crops.warmletter.domain.timeline.dto.response.TimeLineResponse;
import io.crops.warmletter.domain.timeline.enums.AlarmType;
import io.crops.warmletter.domain.timeline.repository.TimeLineRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class TimelineServiceTest {
    @Mock
    private TimeLineRepository timeLineRepository;

    @InjectMocks
    private TimeLineService timeLineService;

    @Test
    @DisplayName("타임라인 조회 성공")
    void get_EventPost_success(){
        // given
        List<TimeLineResponse> timelines = new ArrayList<>();
        TimeLineResponse timeline1 = TimeLineResponse.builder().timelineId(1).title("1111번 편지").alarmType(AlarmType.LETTER).isRead(false).build();
        TimeLineResponse timeline2 = TimeLineResponse.builder().timelineId(2).title("1111번 공유 요청").alarmType(AlarmType.SHARE).isRead(false).build();
        timelines.add(timeline1);
        timelines.add(timeline2);

        when(timeLineRepository.findByMemberId(any(Long.class))).thenReturn(timelines);

        // when
        List<TimeLineResponse> timeLineResponses = timeLineService.getTimelines();

        // then
        assertNotNull(timeLineResponses);
        assertEquals(timeline1.getTimelineId(), timeLineResponses.get(0).getTimelineId());
        assertEquals(timeline1.getTitle(), timeLineResponses.get(0).getTitle());
        assertEquals(timeline1.getAlarmType(), timeLineResponses.get(0).getAlarmType());
        assertEquals(timeline1.isRead(), timeLineResponses.get(0).isRead());
        assertEquals(timeline2.getTimelineId(), timeLineResponses.get(1).getTimelineId());
        assertEquals(timeline2.getTitle(), timeLineResponses.get(1).getTitle());
        assertEquals(timeline2.getAlarmType(), timeLineResponses.get(1).getAlarmType());
        assertEquals(timeline2.isRead(), timeLineResponses.get(1).isRead());
    }
}