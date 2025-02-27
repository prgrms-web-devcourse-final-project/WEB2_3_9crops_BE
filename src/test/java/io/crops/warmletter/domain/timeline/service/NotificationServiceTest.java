package io.crops.warmletter.domain.timeline.service;

import io.crops.warmletter.domain.timeline.dto.response.NotificationResponse;
import io.crops.warmletter.domain.timeline.entity.Timeline;
import io.crops.warmletter.domain.timeline.enums.AlarmType;
import io.crops.warmletter.domain.timeline.repository.TimelineRepository;
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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertNotNull;
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
        long memberId1 = 1L;

        SseEmitter sseEmitter = notificationService.subscribeNotification(memberId1);

        assertNotNull(sseEmitter);
    }

    @Test
    @DisplayName("알림 생성 성공 - LETTER")
    void create_notificationLETTER_success() {
        String zipCode = "12345";
        long receiverId = 1L;
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
        long receiverId = 1L;
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
        long receiverId = 1L;
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
        long receiverId = 1L;
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
        long receiverId = 1L;
        String title = "12345님이 편지를 보냈습니다.";
        String letterId = "1";
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
        long receiverId = 1L;
        String title = "12345님이 편지를 보냈습니다.";
        String letterId = "1";
        AlarmType alarmType = AlarmType.LETTER;

        NotificationResponse notificationResponse = NotificationResponse.builder()
                .title(title)
                .alarmType(alarmType.toString())
                .build();

        notificationService.sendEventToClient(receiverId, notificationResponse);

        verify(emitter, never()).send(any(SseEmitter.SseEventBuilder.class));
    }
}