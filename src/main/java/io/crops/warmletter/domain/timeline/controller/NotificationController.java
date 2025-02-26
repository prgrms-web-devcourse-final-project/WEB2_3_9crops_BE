package io.crops.warmletter.domain.timeline.controller;

import io.crops.warmletter.domain.timeline.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notification")
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping(value = "/sub/{memberId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeNotification(@PathVariable int memberId) {
        return notificationService.subscribeNotification(memberId);
    }
}
