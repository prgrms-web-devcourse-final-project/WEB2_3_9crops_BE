package io.crops.warmletter.domain.badword.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateBadWordRequest {

    @NotBlank(message = "단어는 필수 입력값입니다.")
    private String word;
}
