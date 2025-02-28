package io.crops.warmletter.domain.timeline.facade;

import io.crops.warmletter.domain.timeline.enums.AlarmType;
import io.crops.warmletter.domain.timeline.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationFacade {

    private final NotificationService notificationService;

    public void sendNotification(String senderZipCode, Long receiverId, AlarmType alarmType, String data) {
        notificationService.createNotification(senderZipCode, receiverId, alarmType, data);
    }
}
