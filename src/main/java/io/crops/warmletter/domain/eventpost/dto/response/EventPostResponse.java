package io.crops.warmletter.domain.eventpost.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EventPostResponse {
    private long eventPostId;
    private String title;
}
