package io.crops.warmletter.domain.timeline.controller;

import io.crops.warmletter.domain.timeline.dto.response.TimelineResponse;
import io.crops.warmletter.domain.timeline.service.TimelineService;
import io.crops.warmletter.global.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class TimelineController {
    private final TimelineService timeLineService;

    @GetMapping("/timelines")
    public ResponseEntity<BaseResponse<List<TimelineResponse>>> getTimelines() {
        return ResponseEntity.ok(BaseResponse.of(timeLineService.getTimelines(),"타임라인 조회 성공"));
    }
}
