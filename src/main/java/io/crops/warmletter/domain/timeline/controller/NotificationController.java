package io.crops.warmletter.domain.timeline.controller;

import io.crops.warmletter.domain.timeline.dto.response.ReadNotificationResponse;
import io.crops.warmletter.domain.timeline.service.NotificationService;
import io.crops.warmletter.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
@Tag(name = "Notification", description = "알림 관련 API")
public class NotificationController {
    private final NotificationService notificationService;

    @Operation(summary = "알림 구독", description = "SSE를 이용하여 실시간 알림을 구독합니다.")
    @GetMapping(value = "/sub/{memberId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeNotification(
            @Parameter(description = "구독할 회원 ID", required = true) @PathVariable Long memberId) {
        return notificationService.subscribeNotification(memberId);
    }

    @Operation(summary = "개별 알림 읽음 처리", description = "특정 알림을 읽음 처리합니다.")
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<BaseResponse<ReadNotificationResponse>> updateNotificationRead(
            @Parameter(description = "읽음 처리할 알림 ID", required = true) @PathVariable Long notificationId) {
        return ResponseEntity.ok(BaseResponse.of(notificationService.updateNotificationRead(notificationId),"알림 읽음 처리 성공"));
    }

    @Operation(summary = "모든 알림 읽음 처리", description = "사용자의 모든 알림을 읽음 처리합니다.")
    @PatchMapping("/read")
    public ResponseEntity<BaseResponse<List<ReadNotificationResponse>>> updateNotificationAllRead() {
        return ResponseEntity.ok(BaseResponse.of(notificationService.updateNotificationAllRead(),"모든 알림 읽음 처리 성공"));
    }
}
