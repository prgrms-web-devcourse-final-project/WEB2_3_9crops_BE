package io.crops.warmletter.domain.timeline.dto.request;

import io.crops.warmletter.domain.timeline.enums.AlarmType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationRequest {
    String senderZipCode;
    Long receiverId;
    AlarmType alarmType;
    String data;
}