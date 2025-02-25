package io.crops.warmletter.domain.timeline.dto.response;

import io.crops.warmletter.domain.timeline.enums.AlarmType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TimeLineResponse {
    private long timelineId;
    private String title;
    private AlarmType alarmType;
    private boolean isRead;
}