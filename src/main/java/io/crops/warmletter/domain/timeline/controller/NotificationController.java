package io.crops.warmletter.domain.timeline.controller;

import io.crops.warmletter.domain.timeline.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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
}
