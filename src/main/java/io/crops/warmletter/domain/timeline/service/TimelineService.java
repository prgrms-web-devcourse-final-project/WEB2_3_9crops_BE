package io.crops.warmletter.domain.timeline.service;

import io.crops.warmletter.domain.auth.facade.AuthFacade;
import io.crops.warmletter.domain.timeline.dto.response.ReadNotificationResponse;
import io.crops.warmletter.domain.timeline.dto.response.TimelineResponse;
import io.crops.warmletter.domain.timeline.entity.Timeline;
import io.crops.warmletter.domain.timeline.exception.NotificationNotFoundException;
import io.crops.warmletter.domain.timeline.repository.TimelineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TimelineService {
    private final AuthFacade authFacade;
    private final TimelineRepository timelineRepository;

    @Transactional(readOnly = true)
    public Page<TimelineResponse> getTimelines(Pageable timelinesPageable){
        Long memberId = authFacade.getCurrentUserId();

        // 알람이 없는(empty) 경우도 있어서 예외처리 X
        return timelineRepository.findByMemberId(memberId,timelinesPageable);
    }

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
