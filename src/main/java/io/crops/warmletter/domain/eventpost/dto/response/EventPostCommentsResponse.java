package io.crops.warmletter.domain.eventpost.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventPostCommentsResponse {
    private long commentId;
    private String zipCode;
    private String content;

    @Builder
    public EventPostCommentsResponse(long commentId, String zipCode, String content) {
        this.commentId = commentId;
        this.zipCode = zipCode;
        this.content = content;
    }

}
