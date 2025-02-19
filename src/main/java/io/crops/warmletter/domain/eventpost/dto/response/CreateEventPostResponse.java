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
    private String content;
    private String createdBy;
    private LocalDateTime createdAt;

    @Builder
    public CreateEventPostResponse(Long eventPostId, String title, String content, String createdBy, LocalDateTime createdAt) {
        this.eventPostId = eventPostId;
        this.title = title;
        this.content = content;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }
}
