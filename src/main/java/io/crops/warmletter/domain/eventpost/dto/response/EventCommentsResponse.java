package io.crops.warmletter.domain.eventpost.dto.response;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventCommentsResponse {
    private long commentId;
    private String zipCode;
    private String content;

    @Builder
    public EventCommentsResponse(long commentId, String zipCode, String content) {
        this.commentId = commentId;
        this.zipCode = zipCode;
        this.content = content;
    }
}
