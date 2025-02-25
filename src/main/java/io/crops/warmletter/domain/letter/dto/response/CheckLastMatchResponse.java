package io.crops.warmletter.domain.letter.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CheckLastMatchResponse {

    private final boolean canSend; // 새로운 랜덤 편지를 보낼 수 있는지 여부
    private final LocalDateTime lastMatchedAt; // 마지막 랜덤 편지 발송 시각 (보내지 않았다면 null)

}
