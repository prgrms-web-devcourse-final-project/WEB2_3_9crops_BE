package io.crops.warmletter.domain.eventpost.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EventCommentResponse {
    private long commentId;
    private String content;
}
