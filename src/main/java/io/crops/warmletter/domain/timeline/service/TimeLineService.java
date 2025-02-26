package io.crops.warmletter.domain.timeline.service;

import io.crops.warmletter.domain.auth.facade.AuthFacade;
import io.crops.warmletter.domain.timeline.dto.response.TimeLineResponse;
import io.crops.warmletter.domain.timeline.repository.TimeLineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TimeLineService {
    private final AuthFacade authFacade;
    private final TimeLineRepository timeLineRepository;

    @Transactional(readOnly = true)
    public List<TimeLineResponse> getTimelines(){
        long memberId = 1L; // TODO : authFacade.getCurrentUserId();
        // 알람이 없는(empty) 경우도 있어서 예외처리 X
        List<TimeLineResponse> timeLineResponses = timeLineRepository.findByMemberId(memberId);
        return timeLineResponses;
    }
}
