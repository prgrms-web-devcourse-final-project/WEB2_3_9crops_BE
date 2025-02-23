package io.crops.warmletter.domain.eventpost.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
public class CreateEventPostRequest {

    @NotBlank(message = "제목을 입력해주세요.")
    private String title;
}
