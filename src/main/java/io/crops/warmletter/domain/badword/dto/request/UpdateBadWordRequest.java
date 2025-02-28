package io.crops.warmletter.domain.badword.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBadWordRequest {
    @NotBlank(message = "단어는 필수 입력값입니다.")
    @Schema(description = "등록할 금지어", example = "비속어")
    private String word;
}
