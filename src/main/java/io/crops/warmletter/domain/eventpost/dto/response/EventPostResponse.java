package io.crops.warmletter.domain.eventpost.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EventPostResponse {
    private Long eventPostId;
    private String title;
}
