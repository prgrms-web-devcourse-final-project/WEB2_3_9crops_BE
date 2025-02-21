package io.crops.warmletter.domain.letter.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.crops.warmletter.domain.letter.entity.Letter;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MailboxResponse {

    private final Long letterMatchingId;

    private final String oppositeZipCode;

    private final boolean isActive;

    private final boolean isOppositeRead;

}
