package io.crops.warmletter.domain.letter.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MailboxResponse {

    private final Long letterMatchingId; //편지 매칭 id

    private final String oppositeZipCode; //상대방의 zipcode

    private final boolean isActive; //편지 매칭에서 활성 여부 방이 차단됐는지 안됐는지 true(활성), false(비활성)

    private final boolean isOppositeRead; //편지가 전부 다 읽어졌는지 아닌지 다 읽으면 true 안읽으면 false

    private final Long letterCount; // 총편지 수

    public MailboxResponse(Long letterMatchingId, String oppositeZipCode, boolean isActive, boolean isOppositeRead, Long letterCount) {
        this.letterMatchingId = letterMatchingId;
        this.oppositeZipCode = oppositeZipCode;
        this.isActive = isActive;
        this.isOppositeRead = isOppositeRead;
        this.letterCount = letterCount;
    }
}
