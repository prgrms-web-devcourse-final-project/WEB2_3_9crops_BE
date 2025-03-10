package io.crops.warmletter.domain.timeline.service;

import io.crops.warmletter.domain.auth.facade.AuthFacade;
import io.crops.warmletter.domain.eventpost.dto.response.EventPostsResponse;
import io.crops.warmletter.domain.timeline.dto.response.ReadNotificationResponse;
import io.crops.warmletter.domain.timeline.dto.response.TimelineResponse;
import io.crops.warmletter.domain.timeline.entity.Timeline;
import io.crops.warmletter.domain.timeline.enums.AlarmType;
import io.crops.warmletter.domain.timeline.exception.NotificationNotFoundException;
import io.crops.warmletter.domain.timeline.repository.TimelineRepository;
import io.crops.warmletter.global.error.common.ErrorCode;
import io.crops.warmletter.global.error.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class TimelineServiceTest {
    @Mock
    private TimelineRepository timelineRepository;

    @Mock
    private AuthFacade authFacade;

    @InjectMocks
    private TimelineService timelineService;

    @Test
    @DisplayName("타임라인 조회 성공")
    void get_eventPost_success(){
        // given
        Long memberId = 1L;
        when(authFacade.getCurrentUserId()).thenReturn(memberId);

        TimelineResponse timeline1 = TimelineResponse.builder().timelineId(1L).title("1111번 편지").alarmType(AlarmType.LETTER).isRead(false).build();
        TimelineResponse timeline2 = TimelineResponse.builder().timelineId(2L).title("1111번 공유 요청").alarmType(AlarmType.SHARE).isRead(false).build();

        Pageable pageable = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<TimelineResponse> timelines = List.of(timeline2, timeline1);
        Page<TimelineResponse> timelinesPage = new PageImpl<>(timelines, pageable, timelines.size());

        when(timelineRepository.findByMemberId(any(Long.class),any())).thenReturn(timelinesPage);

        // when
        Page<TimelineResponse> timelineResponse = timelineService.getTimelines(pageable);

        // then
        assertNotNull(timelineResponse);
        assertEquals(timeline2.getTimelineId(), timelineResponse.getContent().get(0).getTimelineId());
        assertEquals(timeline2.getTitle(), timelineResponse.getContent().get(0).getTitle());
        assertEquals(timeline2.getContent(), timelineResponse.getContent().get(0).getContent());
        assertEquals(timeline2.getAlarmType(), timelineResponse.getContent().get(0).getAlarmType());
        assertEquals(1, timelineResponse.getSize());
        assertEquals(2, timelineResponse.getTotalElements());
        assertEquals(2, timelineResponse.getTotalPages());
    }

    @Test
    @DisplayName("알림 읽음 상태 변경 성공 - false 에서 true")
    void update_notificationRead_success(){
        //given
        Long notificationId = 1L;
        Long memberId = 1L;
        when(authFacade.getCurrentUserId()).thenReturn(memberId);

        Timeline timeline = Timeline.builder().memberId(memberId).title("제목").content("내용").alarmType(AlarmType.LETTER).build();
        ReflectionTestUtils.setField(timeline, "id", notificationId);

        when(timelineRepository.findByIdAndMemberId(any(Long.class),any(Long.class))).thenReturn(Optional.of(timeline));

        //when
        ReadNotificationResponse readNotificationResponse = timelineService.updateNotificationRead(notificationId);

        //then
        assertEquals(notificationId, readNotificationResponse.getNotificationId());
        assertTrue(readNotificationResponse.isRead());
    }

    @Test
    @DisplayName("알림 읽음 상태 변경 성공 - 변경 없음")
    void update_notificationAlreadyRead_success(){
        //given
        Long notificationId = 1L;
        Long memberId = 1L;
        when(authFacade.getCurrentUserId()).thenReturn(memberId);

        Timeline timeline = Timeline.builder().memberId(memberId).title("제목").content("내용").alarmType(AlarmType.LETTER).build();
        ReflectionTestUtils.setField(timeline, "id", notificationId);
        ReflectionTestUtils.setField(timeline, "isRead", true);

        when(timelineRepository.findByIdAndMemberId(any(Long.class),any(Long.class))).thenReturn(Optional.of(timeline));

        //when
        ReadNotificationResponse readNotificationResponse = timelineService.updateNotificationRead(notificationId);

        //then
        assertEquals(notificationId, readNotificationResponse.getNotificationId());
        assertTrue(readNotificationResponse.isRead());
    }

    @Test
    @DisplayName("알림 읽음 상태 변경 실패 - 일치하는 notificaitonId 없음 ")
    void update_notification_notFound(){
        //given
        Long memberId = 1L;
        when(authFacade.getCurrentUserId()).thenReturn(memberId);
        when(timelineRepository.findByIdAndMemberId(any(Long.class),any(Long.class))).thenThrow(new NotificationNotFoundException());

        //when
        BusinessException exception = assertThrows(NotificationNotFoundException.class, ()-> timelineService.updateNotificationRead(999L));

        //then
        assertEquals(ErrorCode.NOTIFICATION_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("모든 알림 읽음 상태 변경 성공 - false 에서 true")
    void update_notificationAllRead_success(){
        //given
        Long memberId = 1L;
        when(authFacade.getCurrentUserId()).thenReturn(memberId);

        Timeline timeline1 = Timeline.builder().memberId(memberId).title("제목1").content("내용1").alarmType(AlarmType.LETTER).build();
        ReflectionTestUtils.setField(timeline1, "id", 1L);
        Timeline timeline3 = Timeline.builder().memberId(memberId).title("제목3").content("내용3").alarmType(AlarmType.LETTER).build();
        ReflectionTestUtils.setField(timeline3, "id", 3L);

        List<Timeline> timelines = Arrays.asList(timeline1, timeline3);
        when(timelineRepository.findByMemberIdAndIsReadFalse(any(Long.class))).thenReturn(timelines);

        ReflectionTestUtils.setField(timeline1, "isRead", true);
        ReflectionTestUtils.setField(timeline3, "isRead", true);
        when(timelineRepository.findByIds(any())).thenReturn(timelines);
        //when
        List<ReadNotificationResponse> readNotificationResponse = timelineService.updateNotificationAllRead();

        //then
        assertEquals(1L, readNotificationResponse.get(0).getNotificationId());
        assertTrue(readNotificationResponse.get(0).isRead());
        assertEquals(3L, readNotificationResponse.get(1).getNotificationId());
        assertTrue(readNotificationResponse.get(1).isRead());
    }

    @Test
    @DisplayName("읽지 않은 알림 개수 조회 성공")
    void get_notificationNotRead_success(){
        // given
        int notReadCount = 2;

        when(timelineRepository.countByMemberIdAndIsReadIsFalse(any(Long.class))).thenReturn(notReadCount);

        // when
        Map<String, Integer> timelineResponse = timelineService.getNotificationNotRead();

        // then
        assertNotNull(timelineResponse);
        assertEquals(notReadCount, timelineResponse.get("notReadCount"));
    }
}