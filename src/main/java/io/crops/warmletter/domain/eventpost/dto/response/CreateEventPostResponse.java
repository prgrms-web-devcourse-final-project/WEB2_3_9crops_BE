package io.crops.warmletter.domain.eventpost.dto.response;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class CreateEventPostResponse {
    private long eventPostId;
    private String title;

    @Builder
    public CreateEventPostResponse(Long eventPostId, String title) {
        this.eventPostId = eventPostId;
        this.title = title;
    }
}
