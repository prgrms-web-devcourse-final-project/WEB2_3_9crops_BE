package io.crops.warmletter.domain.member.dto.response;

import io.crops.warmletter.domain.member.enums.SocialProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MeResponse {

    private String zipCode;
    private float temperature;
    private SocialProvider social;
    private String email;
}
