package io.crops.warmletter.domain.timeline.service;

import io.crops.warmletter.domain.auth.facade.AuthFacade;
import io.crops.warmletter.domain.timeline.dto.response.NotificationResponse;
import io.crops.warmletter.domain.timeline.dto.response.ReadNotificationResponse;
import io.crops.warmletter.domain.timeline.entity.Timeline;
import io.crops.warmletter.domain.timeline.enums.AlarmType;
import io.crops.warmletter.domain.timeline.exception.NotificationNotFoundException;
import io.crops.warmletter.domain.timeline.repository.TimelineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final AuthFacade authFacade;
    private final TimelineRepository timelineRepository;

    public SseEmitter subscribeNotification(){
        SseEmitter emitter = new SseEmitter(600_000L); // 10분 후 타임아웃 설정
        Long memberId = authFacade.getCurrentUserId();

        emitters.put(memberId, emitter);

        emitter.onCompletion(() -> emitters.remove(memberId)); // 연결 종료 시 제거
        emitter.onTimeout(() -> emitters.remove(memberId)); // 타임아웃 시 제거

        NotificationResponse notificationResponse = NotificationResponse.builder()
                .title("사용자 " + memberId + " EventStream 생성")
                .alarmType("TEST").build();

        sendEventToClient(memberId, notificationResponse);

        return emitter;
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
            case LETTER:
                builder.title(senderZipCode+"님이 편지를 보냈습니다.");
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

    private void sendEventToClient(Long receiverId, NotificationResponse notificationResponse){
        SseEmitter emitter = emitters.get(receiverId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(notificationResponse, MediaType.APPLICATION_JSON));
            } catch (IOException e) {
                emitters.remove(receiverId);
                emitter.complete();
            }
        }
    }
    @Transactional
    public ReadNotificationResponse updateNotificationRead(Long notificationId){
        Long memberId = authFacade.getCurrentUserId();
        Timeline timeline = timelineRepository.findByIdAndMemberId(notificationId, memberId).orElseThrow(NotificationNotFoundException::new);
        if(!timeline.isRead()){
            timeline.notificationRead();
        }

        return ReadNotificationResponse.builder()
                .notificationId(timeline.getId())
                .isRead(timeline.isRead())
                .build();
    }

    @Transactional
    public List<ReadNotificationResponse> updateNotificationAllRead(){
        Long memberId = authFacade.getCurrentUserId();
        List<Timeline> timelinesBeforeUpdate = timelineRepository.findByMemberIdAndIsReadFalse(memberId);

        // 추후 try catch 로 예외 처리 예정
        timelineRepository.updateIsReadByMemberIdAndIsReadFalse(memberId);

        List<ReadNotificationResponse> timelineResponses = new ArrayList<>();

        List<Long> updatedTimelineIds = timelinesBeforeUpdate.stream()
                .map(Timeline::getId)
                .toList();

        List<Timeline> updatedTimelines = timelineRepository.findByIds(updatedTimelineIds);

        for(Timeline timeline : updatedTimelines ){
            timelineResponses.add(ReadNotificationResponse.builder()
                    .notificationId(timeline.getId())
                    .isRead(timeline.isRead())
                    .build());
        }

        return timelineResponses;
    }
}
