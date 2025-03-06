package io.crops.warmletter.domain.timeline.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LetterAlarmResponse {
    private Long writerId;
    private String zipCode;
}
