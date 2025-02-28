package io.crops.warmletter.domain.badword.dto.response;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class BadWordsResponse {

    @ArraySchema(
            schema = @Schema(description = "등록된 금지어 목록", example = "[\"비속어1\", \"비속어2\", \"비속어3\"]")
    )
    private List<String> words;

}
