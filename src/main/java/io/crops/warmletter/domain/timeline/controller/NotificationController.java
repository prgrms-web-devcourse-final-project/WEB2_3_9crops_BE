package io.crops.warmletter.domain.timeline.controller;

import io.crops.warmletter.domain.timeline.dto.response.ReadNotificationResponse;
import io.crops.warmletter.domain.timeline.service.NotificationService;
import io.crops.warmletter.global.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping(value = "/sub/{memberId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeNotification(@PathVariable Long memberId) {
        return notificationService.subscribeNotification(memberId);
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<BaseResponse<ReadNotificationResponse>> updateNotificationRead(@PathVariable Long notificationId) {
        return ResponseEntity.ok(BaseResponse.of(notificationService.updateNotificationRead(notificationId),"알림 읽음 처리 성공"));
    }

    @PatchMapping("/read")
    public ResponseEntity<BaseResponse<List<ReadNotificationResponse>>> updateNotificationAllRead() {
        return ResponseEntity.ok(BaseResponse.of(notificationService.updateNotificationAllRead(),"모든 알림 읽음 처리 성공"));
    }
}
