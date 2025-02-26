package io.crops.warmletter.domain.timeline.dto.response;

import io.crops.warmletter.domain.timeline.enums.AlarmType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationResponse {
    private String title;
    private String alarmType;
}
