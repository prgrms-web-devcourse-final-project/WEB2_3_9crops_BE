package io.crops.warmletter.domain.timeline.service;

import io.crops.warmletter.domain.auth.exception.UnauthorizedException;
import io.crops.warmletter.domain.auth.facade.AuthFacade;
import io.crops.warmletter.domain.timeline.dto.response.NotificationResponse;
import io.crops.warmletter.domain.timeline.entity.Timeline;
import io.crops.warmletter.domain.timeline.enums.AlarmType;
import io.crops.warmletter.domain.timeline.repository.TimelineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final AuthFacade authFacade;
    private final TimelineRepository timelineRepository;

    public SseEmitter subscribeNotification(){
        Long memberId;
        try {
            memberId = authFacade.getCurrentUserId();
        } catch (UnauthorizedException e){
            log.warn("SSE 구독 실패: 인증되지 않은 사용자");
            SseEmitter emitter = new SseEmitter(0L);
            NotificationResponse notificationResponse = NotificationResponse.builder()
                    .title("Unauthorized")
                    .alarmType("TEST").build();
            try{
                emitter.send(SseEmitter.event()
                        .data(notificationResponse));
            }catch (IOException ioException){
                log.warn("SSE 에러 전송 실패 - Unauthorized");
            }
            emitter.complete();
            return emitter;
        }

        SseEmitter emitter = new SseEmitter(600_000L); // 10분 후 타임아웃 설정

        emitters.put(memberId, emitter);

        emitter.onCompletion(() -> handleCompletion(memberId));
        emitter.onTimeout(() -> handleTimeout(memberId, emitter));

        NotificationResponse notificationResponse = NotificationResponse.builder()
                .title("사용자 " + memberId + " EventStream 생성")
                .alarmType("TEST").build();

        sendEventToClient(memberId, notificationResponse);

        return emitter;
    }

    protected void handleCompletion(Long memberId) {
        emitters.remove(memberId);
        log.info("SSE 연결 종료");
    }

    protected void handleTimeout(Long memberId, SseEmitter emitter) {
        emitters.remove(memberId);
        log.info("SSE 연결 타임아웃 발생");
        emitter.complete();
    }

    // 편지 수신, 신고 조치, 공유 요청, 공유 게시글 등록 시 호출 필요
    @Transactional
    public void createNotification(String senderZipCode, Long receiverId, AlarmType alarmType, String data){
        Timeline.TimelineBuilder builder = Timeline.builder()
                .memberId(receiverId)
                // data = LETTER: letterId / REPORT: adminMemo, 경고횟수 / SHARE: shareProposalId / POSTED: sharePostId
                .content(data)
                .alarmType(alarmType);

        switch(alarmType) {
            case SENDING:
                builder.title(senderZipCode+"님이 편지를 보냈습니다.");
                break;
            case LETTER:
                builder.title(senderZipCode+"님의 편지가 도착했습니다.");
                break;
            case REPORT:
                builder.title("따숨님, 최근 활동에 대해 경고를 받으셨어요.");
                break;
            case SHARE:
                builder.title(senderZipCode+"님이 게시글 공유를 요청했어요.");
                break;
            case POSTED:
                builder.title(senderZipCode+"님과의 대화가 게시판에 공유되었어요.");
                break;

        }

        Timeline timeline = builder.build();
        timelineRepository.save(timeline);

        NotificationResponse notificationResponse = NotificationResponse.builder()
                .title(timeline.getTitle())
                .alarmType(timeline.getAlarmType().toString())
                .build();

        // 알림 전송
        sendEventToClient(receiverId,notificationResponse);
    }

    protected void sendEventToClient(Long receiverId, NotificationResponse notificationResponse){
        SseEmitter emitter = emitters.get(receiverId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .data(notificationResponse, MediaType.APPLICATION_JSON));
                log.info("사용자 ID : {}으로 알림 전송 성공", receiverId);
            } catch (IOException e) {
                emitters.remove(receiverId);
                log.warn("사용자 ID : {}으로 알림 전송 실패",receiverId);
                emitter.complete();
            }
        }
    }

    // 연결을 확인하기 위한 Heartbeat를 30초마다 실행
    @Scheduled(fixedRate = 30000)
    public void sendHeartbeat() {
        NotificationResponse notificationResponse = NotificationResponse.builder()
                .title("heartbeat")
                .alarmType("TEST").build();
        for (Map.Entry<Long, SseEmitter> entry : emitters.entrySet()) {
            Long memberId = entry.getKey();
            SseEmitter emitter = entry.getValue();
            try {
                emitter.send(SseEmitter.event()
                        .data(notificationResponse, MediaType.APPLICATION_JSON));
            } catch (IOException e) {
                emitters.remove(memberId);
                log.warn("사용자 ID : {} 대상 Heartbeat 전송 실패",memberId);
                emitter.complete();
            }
        }
    }
}
