package io.crops.warmletter.domain.letter.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MailboxDetailResponse {


    private final Long letterId;

    private final String title;

    private final boolean myLetter; //내가 보낸 편지인지 확인

    private final boolean active; //활성여부, 신고 여부

    private final LocalDateTime createdAt;

}
