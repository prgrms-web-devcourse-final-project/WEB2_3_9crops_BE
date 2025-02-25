package io.crops.warmletter.domain.eventpost.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EventPostStatusResponse {
    private long eventPostId;
    private Boolean isUsed;
}
