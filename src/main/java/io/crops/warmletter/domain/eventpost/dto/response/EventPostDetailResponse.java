package io.crops.warmletter.domain.eventpost.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
public class EventPostDetailResponse {
    private long eventPostId;
    private String title;
    private List<EventCommentsResponse> eventPostComments;
}
