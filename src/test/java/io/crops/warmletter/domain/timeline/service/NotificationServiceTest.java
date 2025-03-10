package io.crops.warmletter.domain.timeline.service;

import io.crops.warmletter.domain.auth.exception.UnauthorizedException;
import io.crops.warmletter.domain.auth.facade.AuthFacade;
import io.crops.warmletter.domain.timeline.dto.response.NotificationResponse;
import io.crops.warmletter.domain.timeline.entity.Timeline;
import io.crops.warmletter.domain.timeline.enums.AlarmType;
import io.crops.warmletter.domain.timeline.repository.TimelineRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.awaitility.Awaitility.await;
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
    void get_notificationSub_success() throws IOException {
        Long memberId = 1L;
        when(authFacade.getCurrentUserId()).thenReturn(memberId);

        SseEmitter sseEmitter = notificationService.subscribeNotification();

        assertNotNull(sseEmitter);
        assertTrue(emitters.containsKey(memberId));
    }

    @Test
    @DisplayName("연결이 종료 성공")
    void test_sseEmitter_onCompletion() {
        // Given
        Long memberId = 1L;

        SseEmitter emitter = new SseEmitter(600_000L);
        emitters.put(memberId, emitter);

        // When: 분리한 handleTimeout 메서드 직접 호출
        notificationService.handleCompletion(memberId);

        // Then: emitters에서 제거되었는지 확인
        assertFalse(emitters.containsKey(memberId));
    }

    @Test
    @DisplayName("SSE가 타임아웃될 때 onTimeout이 호출")
    void test_sseEmitter_onTimeout() throws InterruptedException {
        // Given
        Long memberId = 1L;

        SseEmitter emitter = new SseEmitter(600_000L);
        emitters.put(memberId, emitter);

        // When: 분리한 handleTimeout 메서드 직접 호출
        notificationService.handleTimeout(memberId, emitter);

        // Then: emitters에서 제거되었는지 확인
        assertFalse(emitters.containsKey(memberId));
    }

    @Test
    @DisplayName("인증되지 않은 사용자가 SSE 구독 시 Unauthorized 응답 반환")
    void test_unauthorizedUser_returnsUnauthorizedSseEmitter() throws IOException {
        // Given
        when(authFacade.getCurrentUserId()).thenThrow(new UnauthorizedException());
        // When
        SseEmitter sseEmitter = notificationService.subscribeNotification();

        // Then
        assertNotNull(sseEmitter);
        assertEquals(0L, sseEmitter.getTimeout());
        assertTrue(emitters.isEmpty());

    }

    @Test
    @DisplayName("알림 생성 성공 - SENDING")
    void create_notificationSENDING_success() {
        String zipCode = "12345";
        Long receiverId = 1L;
        AlarmType alarmType = AlarmType.SENDING;
        String letterId = "1";

        Timeline timeline = Timeline.builder()
                .memberId(receiverId)
                .title(zipCode+"님이 편지를 보냈습니다.")
                .content(letterId)
                .alarmType(alarmType)
                .build();

        when(timelineRepository.save(any(Timeline.class))).thenReturn(timeline);

        notificationService.createNotification(zipCode,receiverId, AlarmType.SENDING, letterId);
    }

    @Test
    @DisplayName("알림 생성 성공 - LETTER")
    void create_notificationLETTER_success() {
        String zipCode = "12345";
        Long receiverId = 1L;
        AlarmType alarmType = AlarmType.LETTER;
        String letterId = null;

        Timeline timeline = Timeline.builder()
                .memberId(receiverId)
                .title(zipCode+"님의 편지가 도착했습니다.")
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
        String senderZipCode = "11111";
        Long receiverId = 1L;
        AlarmType alarmType = AlarmType.LETTER;
        String data = "1";

        emitters.put(receiverId, emitter);

        doNothing().when(emitter).send(any(SseEmitter.SseEventBuilder.class));

        notificationService.createNotification(senderZipCode,receiverId,alarmType,data);

        // Then
        verify(emitter, times(1)).send(any(SseEmitter.SseEventBuilder.class));
    }

    @Test
    @DisplayName("알림 전송 실패 - 일치하는 receiverId 없음")
    void create_sendEventToClient_notExistsReceiverId() throws IOException {
        String senderZipCode = "11111";
        Long receiverId = 1L;
        AlarmType alarmType = AlarmType.LETTER;
        String data = "1";

        notificationService.createNotification(senderZipCode,receiverId,alarmType,data);

        verify(emitter, never()).send(any(SseEmitter.SseEventBuilder.class));
    }

    @Test
    @DisplayName("알림 전송 실패 - IOException")
    void create_sendEventToClient_exception() throws IOException {
        // given
        String senderZipCode = "11111";
        Long receiverId = 1L;
        AlarmType alarmType = AlarmType.LETTER;
        String data = "1";

        Long memberId1 = 1L;

        // emitter1이 예외를 던지도록 설정
        doThrow(new IOException()).when(emitter).send(any(SseEmitter.SseEventBuilder.class));

        emitters.put(memberId1, emitter);;

        // when
        notificationService.createNotification(senderZipCode,receiverId,alarmType,data);

        // then
        // emitter1.send()가 호출되었는지 확인
        verify(emitter).send(any(SseEmitter.SseEventBuilder.class));
        // 예외 발생 후 emitter1.complete()가 호출되었는지 확인
        verify(emitter).complete();
        // emitter1이 emitters에서 제거되었는지 확인
        assertThat(emitters).hasSize(0);
        assertThat(emitters).doesNotContainKey(memberId1);

        // emitter1.send()는 정상적으로 호출되었는지 확인
        Mockito.verify(emitter).send(Mockito.any(SseEmitter.SseEventBuilder.class));
    }

    @Test
    @DisplayName("하트비트 전송 성공")
    void send_heartbeat_success() throws IOException {
        // given
        Long memberId1 = 1L;
        Long memberId2 = 2L;

        SseEmitter emitter1 = Mockito.mock(SseEmitter.class);
        SseEmitter emitter2 = Mockito.mock(SseEmitter.class);

        emitters.put(memberId1, emitter1);
        emitters.put(memberId2, emitter2);

        // when
        notificationService.sendHeartbeat();

        // then
        // SseEmitter.SseEventBuilder를 사용하는 send 메서드를 명시적으로 검증
        ArgumentCaptor<SseEmitter.SseEventBuilder> captor = ArgumentCaptor.forClass(SseEmitter.SseEventBuilder.class);

        Mockito.verify(emitter1).send(captor.capture());
        Mockito.verify(emitter2).send(captor.capture());

        // 캡처된 인자들 검사 (선택사항)
        List<SseEmitter.SseEventBuilder> capturedBuilders = captor.getAllValues();
        assertThat(capturedBuilders).hasSize(2);
    }

    @Test
    @DisplayName("하트비트 전송 실패 - 이미터가 비어있을 때")
    void send_heartbeat_notExistsEmitter() {
        // given
        // emitters가 비어 있는 상태

        // when & then
        // 예외가 발생하지 않고 정상적으로 실행되어야 함
        assertThatCode(() -> notificationService.sendHeartbeat())
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("하트비트 전송 실패 - 하트비트 전송 중 예외 발생 시 이미터 제거")
    void sendHeartbeat_IOExcep() throws IOException {
        // given
        Long memberId1 = 1L;
        Long memberId2 = 2L;

        SseEmitter emitter1 = Mockito.mock(SseEmitter.class);
        SseEmitter emitter2 = Mockito.mock(SseEmitter.class);

        // emitter1이 예외를 던지도록 설정
        Mockito.doThrow(new IOException()).when(emitter1).send(Mockito.any(SseEmitter.SseEventBuilder.class));

        emitters.put(memberId1, emitter1);
        emitters.put(memberId2, emitter2);

        // when
        notificationService.sendHeartbeat();

        // then
        // emitter1.send()가 호출되었는지 확인
        Mockito.verify(emitter1).send(Mockito.any(SseEmitter.SseEventBuilder.class));
        // 예외 발생 후 emitter1.complete()가 호출되었는지 확인
        Mockito.verify(emitter1).complete();
        // emitter1이 emitters에서 제거되었는지 확인
        assertThat(emitters).hasSize(1);
        assertThat(emitters).containsKey(memberId2);
        assertThat(emitters).doesNotContainKey(memberId1);

        // emitter2.send()는 정상적으로 호출되었는지 확인
        Mockito.verify(emitter2).send(Mockito.any(SseEmitter.SseEventBuilder.class));
    }
}