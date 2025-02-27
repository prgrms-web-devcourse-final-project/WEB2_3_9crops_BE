package io.crops.warmletter.domain.timeline.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReadNotificationResponse {
    private long notificationId;
    private Boolean isRead;
}
