package io.crops.warmletter.domain.eventpost.dto.response;

import io.crops.warmletter.global.response.PageResponse;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EventPostDetailResponse {
    private Long eventPostId;
    private String title;
    private PageResponse<EventCommentsResponse> eventPostComments;
}
