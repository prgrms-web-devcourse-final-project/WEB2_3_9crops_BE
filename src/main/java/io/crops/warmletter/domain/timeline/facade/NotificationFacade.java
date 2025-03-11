package io.crops.warmletter.domain.timeline.facade;

import io.crops.warmletter.domain.timeline.dto.request.NotificationRequest;
import io.crops.warmletter.domain.timeline.enums.AlarmType;
import io.crops.warmletter.domain.timeline.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class NotificationFacade {

    private final NotificationService notificationService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendNotification(NotificationRequest notificationRequest) {
        notificationService.createNotification(
                notificationRequest.getSenderZipCode(),
                notificationRequest.getReceiverId(),
                notificationRequest.getAlarmType(),
                notificationRequest.getData());
    }
}
