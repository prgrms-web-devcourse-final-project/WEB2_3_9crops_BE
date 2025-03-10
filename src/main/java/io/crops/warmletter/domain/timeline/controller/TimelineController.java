package io.crops.warmletter.domain.timeline.controller;

import io.crops.warmletter.domain.timeline.dto.response.ReadNotificationResponse;
import io.crops.warmletter.domain.timeline.dto.response.TimelineResponse;
import io.crops.warmletter.domain.timeline.service.TimelineService;
import io.crops.warmletter.global.response.BaseResponse;
import io.crops.warmletter.global.response.PageResponse;
import io.crops.warmletter.global.util.PageableConverter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    @PatchMapping("/notifications/{notificationId}/read")
    @Operation(summary = "알림 읽음 처리", description = "로그인한 사용자의 특정 알림을 읽음 처리합니다.")
    public ResponseEntity<BaseResponse<ReadNotificationResponse>> updateNotificationRead(@PathVariable Long notificationId) {
        return ResponseEntity.ok(BaseResponse.of(timeLineService.updateNotificationRead(notificationId),"알림 읽음 처리 성공"));
    }

    @PatchMapping("/notifications/read")
    @Operation(summary = "모든 알림 읽음 처리", description = "로그인한 사용자의 모든 알림을 읽음 처리합니다.")
    public ResponseEntity<BaseResponse<List<ReadNotificationResponse>>> updateNotificationAllRead() {
        return ResponseEntity.ok(BaseResponse.of(timeLineService.updateNotificationAllRead(),"모든 알림 읽음 처리 성공"));
    }

    @GetMapping("/notifications/not-read")
    @Operation(summary = "읽지 않은 알림 개수 조회", description = "로그인한 사용자의 읽지 않은 알림 개수를 조회합니다.")
    public ResponseEntity<BaseResponse<Map<String,Integer>>> getNotificationsNotRead() {
        return ResponseEntity.ok(BaseResponse.of(timeLineService.getNotificationNotRead(),"읽지 않은 알림 개수 조회 성공"));
    }
}
