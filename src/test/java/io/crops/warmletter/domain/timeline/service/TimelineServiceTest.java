package io.crops.warmletter.domain.timeline.service;

import io.crops.warmletter.domain.auth.facade.AuthFacade;
import io.crops.warmletter.domain.timeline.dto.response.TimelineResponse;
import io.crops.warmletter.domain.timeline.enums.AlarmType;
import io.crops.warmletter.domain.timeline.repository.TimelineRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class TimelineServiceTest {
    @Mock
    private TimelineRepository timeLineRepository;

    @Mock
    private AuthFacade authFacade;

    @InjectMocks
    private TimelineService timeLineService;

    @Test
    @DisplayName("타임라인 조회 성공")
    void get_EventPost_success(){
        // given
        Long memberId = 1L;
        when(authFacade.getCurrentUserId()).thenReturn(memberId);

        List<TimelineResponse> timelines = new ArrayList<>();
        TimelineResponse timeline1 = TimelineResponse.builder().timelineId(1L).title("1111번 편지").alarmType(AlarmType.LETTER).isRead(false).build();
        TimelineResponse timeline2 = TimelineResponse.builder().timelineId(2L).title("1111번 공유 요청").alarmType(AlarmType.SHARE).isRead(false).build();
        timelines.add(timeline1);
        timelines.add(timeline2);

        when(timeLineRepository.findByMemberId(any(Long.class))).thenReturn(timelines);

        // when
        List<TimelineResponse> timelineRespons = timeLineService.getTimelines();

        // then
        assertNotNull(timelineRespons);
        assertEquals(timeline1.getTimelineId(), timelineRespons.get(0).getTimelineId());
        assertEquals(timeline1.getTitle(), timelineRespons.get(0).getTitle());
        assertEquals(timeline1.getAlarmType(), timelineRespons.get(0).getAlarmType());
        assertEquals(timeline1.isRead(), timelineRespons.get(0).isRead());
        assertEquals(timeline2.getTimelineId(), timelineRespons.get(1).getTimelineId());
        assertEquals(timeline2.getTitle(), timelineRespons.get(1).getTitle());
        assertEquals(timeline2.getAlarmType(), timelineRespons.get(1).getAlarmType());
        assertEquals(timeline2.isRead(), timelineRespons.get(1).isRead());
    }
}