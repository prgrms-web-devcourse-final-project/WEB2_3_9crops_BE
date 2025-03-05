package io.crops.warmletter.domain.timeline.service;

import io.crops.warmletter.domain.auth.facade.AuthFacade;
import io.crops.warmletter.domain.eventpost.dto.response.EventPostsResponse;
import io.crops.warmletter.domain.timeline.dto.response.TimelineResponse;
import io.crops.warmletter.domain.timeline.enums.AlarmType;
import io.crops.warmletter.domain.timeline.repository.TimelineRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.test.context.ActiveProfiles;

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

        TimelineResponse timeline1 = TimelineResponse.builder().timelineId(1L).title("1111번 편지").alarmType(AlarmType.LETTER).isRead(false).build();
        TimelineResponse timeline2 = TimelineResponse.builder().timelineId(2L).title("1111번 공유 요청").alarmType(AlarmType.SHARE).isRead(false).build();

        Pageable pageable = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<TimelineResponse> timelines = List.of(timeline2, timeline1);
        Page<TimelineResponse> timelinesPage = new PageImpl<>(timelines, pageable, timelines.size());

        when(timeLineRepository.findByMemberId(any(Long.class),any())).thenReturn(timelinesPage);

        // when
        Page<TimelineResponse> timelineResponse = timeLineService.getTimelines(pageable);

        // then
        assertNotNull(timelineResponse);
        assertEquals(timeline2.getTimelineId(), timelineResponse.getContent().get(0).getTimelineId());
        assertEquals(timeline2.getTitle(), timelineResponse.getContent().get(0).getTitle());
        assertEquals(timeline2.getAlarmType(), timelineResponse.getContent().get(0).getAlarmType());
        assertEquals(1, timelineResponse.getSize());
        assertEquals(2, timelineResponse.getTotalElements());
        assertEquals(2, timelineResponse.getTotalPages());

    }
}