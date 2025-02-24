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
    //todo 보낸사람(편지쓴사람)도 같이 보내줘야 될듯????
    private final Long letterId;

    private final String content;

    private final String zipCode;

    private final Category category;

    private final PaperType paperType;

    private final FontType fontType;

    private final LocalDateTime createdAt;

    public RandomLetterResponse(Long letterId, String content, String zipCode, Category category, PaperType paperType, FontType fontType, LocalDateTime createdAt) {
        this.letterId = letterId;
        this.content = content;
        this.zipCode = zipCode;
        this.category = category;
        this.paperType = paperType;
        this.fontType = fontType;
        this.createdAt = createdAt;
    }
}
