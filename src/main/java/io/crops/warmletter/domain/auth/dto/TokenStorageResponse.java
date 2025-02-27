package io.crops.warmletter.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class TokenStorageResponse {

    private String accessToken;
    private boolean hasZipCode;
    private Long userId;
}
