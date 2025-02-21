package io.crops.warmletter.domain.eventpost.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateEventPostRequest {

    @NotBlank(message = "제목을 입력해주세요.")
    private String title;

    @Builder
    public CreateEventPostRequest(String title) {
        this.title = title;
    }
}
