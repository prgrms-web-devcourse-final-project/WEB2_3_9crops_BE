package io.crops.warmletter.domain.eventpost.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EventCommentResponse {
    private Long commentId;
    private String content;
}
