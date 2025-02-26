package io.crops.warmletter.domain.member.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TemperaturePolicy {

    LETTER_CREATION(0.5f),
    GOOD_EVALUATION(1.0f),
    BAD_EVALUATION(-1.0f);

    private final float value;
}
