package io.crops.warmletter.domain.member.entity;

import io.crops.warmletter.domain.member.exception.InvalidTemperatureException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class TemperatureTest {

    @DisplayName("온도 증가 시 0 이하의 값이면 예외가 발생한다")
    @ParameterizedTest
    @ValueSource(floats = {0.0f, -1.0f, -5.5f})
    void increase_InvalidAmount(float invalidAmount) {
        // given
        Temperature temperature = new Temperature(30.0f);

        // when & then
        assertThrows(InvalidTemperatureException.class,
                () -> temperature.increase(invalidAmount));
    }

    @DisplayName("온도 감소 시 0 이하의 값이면 예외가 발생한다")
    @ParameterizedTest
    @ValueSource(floats = {0.0f, -1.0f, -5.5f})
    void decrease_InvalidAmount(float invalidAmount) {
        // given
        Temperature temperature = new Temperature(30.0f);

        // when & then
        assertThrows(InvalidTemperatureException.class,
                () -> temperature.decrease(invalidAmount));
    }
}