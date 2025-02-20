package io.crops.warmletter.domain.badword.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBadWordStatusRequest {

    @JsonProperty("isUsed")
    private boolean isUsed;
}
