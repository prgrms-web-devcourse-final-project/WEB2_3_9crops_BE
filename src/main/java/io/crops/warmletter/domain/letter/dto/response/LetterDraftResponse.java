package io.crops.warmletter.domain.letter.dto.response;

import io.crops.warmletter.domain.letter.enums.Category;
import io.crops.warmletter.domain.letter.enums.FontType;
import io.crops.warmletter.domain.letter.enums.PaperType;
import io.crops.warmletter.domain.letter.enums.Status;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class LetterDraftResponse {

    private final Long letterId;
    private final Long writerId;
    private final Long receiverId;
    private final Long parentLetterId;
    private final String title;
    private final String content;
    private final Category category;
    private final PaperType paperType;
    private final FontType fontType;
    private final Status status;
    private final LocalDateTime deliveryStartedAt;
    private final LocalDateTime deliveryCompletedAt;
    private final Long matchingId;

}
