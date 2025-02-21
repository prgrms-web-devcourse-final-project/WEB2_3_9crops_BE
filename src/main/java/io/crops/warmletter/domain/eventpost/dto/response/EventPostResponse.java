package io.crops.warmletter.domain.eventpost.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventPostResponse {
    private long eventPostId;
    private String title;

    @Builder
    public EventPostResponse(Long eventPostId, String title) {
        this.eventPostId = eventPostId;
        this.title = title;
    }
}
