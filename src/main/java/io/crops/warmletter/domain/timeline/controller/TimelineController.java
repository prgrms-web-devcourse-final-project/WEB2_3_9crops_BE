package io.crops.warmletter.domain.timeline.controller;

import io.crops.warmletter.domain.timeline.dto.response.TimelineResponse;
import io.crops.warmletter.domain.timeline.service.TimelineService;
import io.crops.warmletter.global.response.BaseResponse;
import io.crops.warmletter.global.response.PageResponse;
import io.crops.warmletter.global.util.PageableConverter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "타임라인 기능 API", description = "타임라인 조회 기능의 API를 제공합니다.")
public class TimelineController {
    private final TimelineService timeLineService;

    @GetMapping("/timelines")
    @Operation(summary = "타임라인 조회", description = "로그인한 사용자의 모든 타임라인을 조회합니다.")
    public ResponseEntity<BaseResponse<PageResponse<TimelineResponse>>> getTimelines(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Pageable timelinesPageable = PageableConverter.convertToPageable(pageable);

        return ResponseEntity.ok(BaseResponse.of(new PageResponse<>(timeLineService.getTimelines(timelinesPageable)),"타임라인 조회 성공"));
    }
}
