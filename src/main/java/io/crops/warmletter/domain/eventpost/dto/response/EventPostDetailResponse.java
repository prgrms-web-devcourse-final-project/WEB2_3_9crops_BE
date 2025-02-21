package io.crops.warmletter.domain.eventpost.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventPostDetailResponse {
    private long eventPostId;
    private String title;
    private List<EventPostCommentsResponse> eventPostComments;

    @Builder
    public EventPostDetailResponse(long eventPostId, String title, List<EventPostCommentsResponse> eventPostComments) {
        this.eventPostId = eventPostId;
        this.title = title;
        this.eventPostComments = eventPostComments;
    }
}
