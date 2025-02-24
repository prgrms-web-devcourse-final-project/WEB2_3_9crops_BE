package io.crops.warmletter.domain.eventpost.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateEventCommentRequest {
    @NotBlank(message = "내용을 입력해주세요.")
    private String content;
}
