package io.crops.warmletter.domain.timeline.service;

import io.crops.warmletter.domain.auth.exception.UnauthorizedException;
import io.crops.warmletter.domain.auth.facade.AuthFacade;
import io.crops.warmletter.domain.timeline.dto.response.NotificationResponse;
import io.crops.warmletter.domain.timeline.dto.response.ReadNotificationResponse;
import io.crops.warmletter.domain.timeline.entity.Timeline;
import io.crops.warmletter.domain.timeline.enums.AlarmType;
import io.crops.warmletter.domain.timeline.exception.NotificationNotFoundException;
import io.crops.warmletter.domain.timeline.repository.TimelineRepository;
import io.crops.warmletter.global.error.common.ErrorCode;
import io.crops.warmletter.global.error.exception.AuthException;
import io.crops.warmletter.global.error.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {
    @Mock
    private TimelineRepository timelineRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Mock
    private AuthFacade authFacade;

    @Mock
    private SseEmitter emitter;

    private Map<Long, SseEmitter> emitters;

    @BeforeEach
    void setUp() {
        // emitters를 Mock으로 초기화하여 직접 설정할 수 있도록 합니다.
        emitters = new ConcurrentHashMap<>();

        // NotificationService의 내부 emitters를 Mock으로 설정
        notificationService = Mockito.spy(notificationService);

        // emitters를 mock으로 주입하는 방식으로 테스트
        ReflectionTestUtils.setField(notificationService, "emitters", emitters);
    }


    @Test
    @DisplayName("알림 구독 생성 성공")
    void get_notificationSub_success() {
        Long memberId = 1L;
        when(authFacade.getCurrentUserId()).thenReturn(memberId);

        SseEmitter sseEmitter = notificationService.subscribeNotification();

        assertNotNull(sseEmitter);
    }

    @Test
    @DisplayName("알림 생성 성공 - LETTER")
    void create_notificationLETTER_success() {
        String zipCode = "12345";
        Long receiverId = 1L;
        AlarmType alarmType = AlarmType.LETTER;
        String letterId = "1";

        Timeline timeline = Timeline.builder()
                .memberId(receiverId)
                .title(zipCode+"님이 편지를 보냈습니다.")
                .content(letterId)
                .alarmType(alarmType)
                .build();

        when(timelineRepository.save(any(Timeline.class))).thenReturn(timeline);

        notificationService.createNotification(zipCode,receiverId, AlarmType.LETTER, letterId);
    }

    @Test
    @DisplayName("알림 생성 성공 - REPORT")
    void create_notificationREPORT_success() {
        String zipCode = "12345";
        Long receiverId = 1L;
        AlarmType alarmType = AlarmType.REPORT;
        String letterId = "1";

        Timeline timeline = Timeline.builder()
                .memberId(receiverId)
                .title("따숨님, 최근 활동에 대해 경고를 받으셨어요.")
                .content(letterId)
                .alarmType(alarmType)
                .build();

        when(timelineRepository.save(any(Timeline.class))).thenReturn(timeline);

        notificationService.createNotification(zipCode,receiverId, AlarmType.REPORT, letterId);
    }
    @Test
    @DisplayName("알림 생성 성공 - SHARE")
    void create_notificationSHARE_success() {
        String zipCode = "12345";
        Long receiverId = 1L;
        AlarmType alarmType = AlarmType.SHARE;
        String letterId = "1";

        Timeline timeline = Timeline.builder()
                .memberId(receiverId)
                .title(zipCode+"님이 게시글 공유를 요청했어요.")
                .content(letterId)
                .alarmType(alarmType)
                .build();

        when(timelineRepository.save(any(Timeline.class))).thenReturn(timeline);

        notificationService.createNotification(zipCode,receiverId, AlarmType.SHARE, letterId);
    }

    @Test
    @DisplayName("알림 생성 성공 - POSTED")
    void create_notificationPOSTED_success() {
        String zipCode = "12345";
        Long receiverId = 1L;
        AlarmType alarmType = AlarmType.POSTED;
        String letterId = "1";

        Timeline timeline = Timeline.builder()
                .memberId(receiverId)
                .title(zipCode+"님과의 대화가 게시판에 공유되었어요.")
                .content(letterId)
                .alarmType(alarmType)
                .build();

        when(timelineRepository.save(any(Timeline.class))).thenReturn(timeline);

        notificationService.createNotification(zipCode,receiverId, AlarmType.POSTED, letterId);
    }

    @Test
    @DisplayName("알림 전송 성공")
    void create_sendEventToClient_success() throws IOException{
        Long receiverId = 1L;
        String title = "12345님이 편지를 보냈습니다.";
        AlarmType alarmType = AlarmType.LETTER;

        NotificationResponse notificationResponse = NotificationResponse.builder()
                .title(title)
                .alarmType(alarmType.toString())
                .build();

        emitters.put(receiverId, emitter);

        doNothing().when(emitter).send(any(SseEmitter.SseEventBuilder.class));

        notificationService.sendEventToClient(receiverId, notificationResponse);

        // Then
        verify(emitter, times(1)).send(any(SseEmitter.SseEventBuilder.class));
    }

    @Test
    @DisplayName("알림 전송 실패 - 일치하는 receiverId 없음")
    void create_sendEventToClient_notExistsReceiverId() throws IOException {
        Long receiverId = 1L;
        String title = "12345님이 편지를 보냈습니다.";
        AlarmType alarmType = AlarmType.LETTER;

        NotificationResponse notificationResponse = NotificationResponse.builder()
                .title(title)
                .alarmType(alarmType.toString())
                .build();

        notificationService.sendEventToClient(receiverId, notificationResponse);

        verify(emitter, never()).send(any(SseEmitter.SseEventBuilder.class));
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
        ReadNotificationResponse readNotificationResponse = notificationService.updateNotificationRead(notificationId);

        //then
        assertEquals(notificationId, readNotificationResponse.getNotificationId());
        assertTrue(readNotificationResponse.getIsRead());
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
        ReadNotificationResponse readNotificationResponse = notificationService.updateNotificationRead(notificationId);

        //then
        assertEquals(notificationId, readNotificationResponse.getNotificationId());
        assertTrue(readNotificationResponse.getIsRead());
    }

    @Test
    @DisplayName("알림 읽음 상태 변경 실패 - 일치하는 notificaitonId 없음 ")
    void update_notification_notFound(){
        //given
        Long memberId = 1L;
        when(authFacade.getCurrentUserId()).thenReturn(memberId);
        when(timelineRepository.findByIdAndMemberId(any(Long.class),any(Long.class))).thenThrow(new NotificationNotFoundException());

        //when
        BusinessException exception = assertThrows(NotificationNotFoundException.class, ()-> notificationService.updateNotificationRead(999L));

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

        //when
        List<ReadNotificationResponse> readNotificationResponse = notificationService.updateNotificationAllRead();

        //then
        assertEquals(1L, readNotificationResponse.get(0).getNotificationId());
        assertTrue(readNotificationResponse.get(0).getIsRead());
        assertEquals(3L, readNotificationResponse.get(1).getNotificationId());
        assertTrue(readNotificationResponse.get(1).getIsRead());
    }

}