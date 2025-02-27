package io.crops.warmletter.domain.member.entity;

import io.crops.warmletter.domain.member.enums.TemperaturePolicy;
import io.crops.warmletter.domain.member.exception.InvalidTemperatureException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Temperature {

    private static final float MAX_VALUE = 100.0f;
    private static final float MIN_VALUE = 0.0f;

    @Column(name = "temperature", nullable = false)
    private float value;

    public Temperature(float initialValue) {
        setValue(initialValue);
    }

    public float getValue() {
        return value;
    }

    private static void validateInputTemperature(float amount) {
        if (amount <= 0) {
            throw new InvalidTemperatureException();
        }
    }

    private void setValue(float value) {
        if (value > MAX_VALUE) {
            this.value = MAX_VALUE;
        } else if (value < MIN_VALUE) {
            this.value = MIN_VALUE;
        } else {
            this.value = value;
        }
    }

    public void increase(float amount) {
        validateInputTemperature(amount);
        setValue(this.value + amount);
    }

    public void decrease(float amount) {
        validateInputTemperature(amount);
        setValue(this.value - amount);
    }

    public void applyPolicy(TemperaturePolicy policy) {
        float change = policy.getValue();
        if (change > 0) {
            increase(change);
        } else if (change < 0) {
            decrease(Math.abs(change));
        }
    }
}
