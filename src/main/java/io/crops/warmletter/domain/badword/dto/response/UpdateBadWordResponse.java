package io.crops.warmletter.domain.badword.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UpdateBadWordResponse {

    @Schema(description = "금지어 단어", example = "비속어")
    private String word;
}
