package io.crops.warmletter.domain.letter.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.crops.warmletter.domain.letter.enums.Category;
import io.crops.warmletter.domain.letter.enums.FontType;
import io.crops.warmletter.domain.letter.enums.PaperType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RandomLetterResponse {
    private final Long letterId;

    private final Long writerId;

    private final String title;

    private final String zipCode;

    private final Category category;

    private final LocalDateTime createdAt;

    public RandomLetterResponse(Long letterId, Long writerId, String title, String zipCode, Category category, LocalDateTime createdAt) {
        this.letterId = letterId;
        this.writerId = writerId;
        this.title = title;
        this.zipCode = zipCode;
        this.category = category;
        this.createdAt = createdAt;
    }
}
