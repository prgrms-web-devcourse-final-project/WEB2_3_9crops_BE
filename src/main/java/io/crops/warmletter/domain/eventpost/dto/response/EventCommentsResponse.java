package io.crops.warmletter.domain.eventpost.dto.response;

import lombok.*;

@Getter
@Builder
public class EventCommentsResponse {
    private Long commentId;
    private String zipCode;
    private String content;
}
