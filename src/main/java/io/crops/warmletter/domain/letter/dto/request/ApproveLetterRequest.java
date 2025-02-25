package io.crops.warmletter.domain.letter.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApproveLetterRequest {

    private Long letterId;

    private Long writerId;

}
