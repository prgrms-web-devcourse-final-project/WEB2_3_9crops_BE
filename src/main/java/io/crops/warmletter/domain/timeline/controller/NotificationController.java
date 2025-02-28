package io.crops.warmletter.domain.timeline.controller;

import io.crops.warmletter.domain.timeline.dto.response.ReadNotificationResponse;
import io.crops.warmletter.domain.timeline.service.NotificationService;
import io.crops.warmletter.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
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
@Tag(name = "알림 기능 API", description = "알림 구독, 알림 읽음 처리 기능의 API를 제공합니다.")
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping(value = "/sub", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "알림 구독", description = "SSE를 사용하여 로그인한 사용자 간 실시간 알림을 구독합니다.")
    public SseEmitter subscribeNotification() {
        return notificationService.subscribeNotification();
    }

    @PatchMapping("/{notificationId}/read")
    @Operation(summary = "알림 읽음 처리", description = "로그인한 사용자의 특정 알림을 읽음 처리합니다.")
    public ResponseEntity<BaseResponse<ReadNotificationResponse>> updateNotificationRead(@PathVariable Long notificationId) {
        return ResponseEntity.ok(BaseResponse.of(notificationService.updateNotificationRead(notificationId),"알림 읽음 처리 성공"));
    }

    @PatchMapping("/read")
    @Operation(summary = "모든 알림 읽음 처리", description = "로그인한 사용자의 모든 알림을 읽음 처리합니다.")
    public ResponseEntity<BaseResponse<List<ReadNotificationResponse>>> updateNotificationAllRead() {
        return ResponseEntity.ok(BaseResponse.of(notificationService.updateNotificationAllRead(),"모든 알림 읽음 처리 성공"));
    }
}
