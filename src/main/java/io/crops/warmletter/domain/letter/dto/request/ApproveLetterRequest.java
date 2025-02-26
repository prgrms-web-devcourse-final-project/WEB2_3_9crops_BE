package io.crops.warmletter.domain.letter.dto.request;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApproveLetterRequest {

    private Long letterId;

    private Long writerId;

    @Builder
    public ApproveLetterRequest(Long letterId, Long writerId) {
        this.letterId = letterId;
        this.writerId = writerId;
    }
}
