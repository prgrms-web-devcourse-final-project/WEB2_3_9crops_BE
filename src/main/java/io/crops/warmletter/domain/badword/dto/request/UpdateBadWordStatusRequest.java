package io.crops.warmletter.domain.badword.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBadWordStatusRequest {
    @NotNull(message = "상태값은 필수입니다.")
    private Boolean isUsed;
}
