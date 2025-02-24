package io.crops.warmletter.domain.eventpost.dto.response;

import lombok.*;

@Getter
@Builder
public class EventCommentsResponse {
    private long commentId;
    private String zipCode;
    private String content;
}
