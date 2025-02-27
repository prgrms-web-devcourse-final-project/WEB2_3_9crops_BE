package io.crops.warmletter.domain.timeline.service;

import io.crops.warmletter.domain.auth.facade.AuthFacade;
import io.crops.warmletter.domain.timeline.dto.response.TimelineResponse;
import io.crops.warmletter.domain.timeline.repository.TimelineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TimelineService {
    private final AuthFacade authFacade;
    private final TimelineRepository timeLineRepository;

    @Transactional(readOnly = true)
    public List<TimelineResponse> getTimelines(){
        long memberId = 1L; // TODO : authFacade.getCurrentUserId();
        // 알람이 없는(empty) 경우도 있어서 예외처리 X
        List<TimelineResponse> timelineResponse = timeLineRepository.findByMemberId(memberId);
        return timelineResponse;
    }
}
