package io.crops.warmletter.domain.calculator;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CalculatorTest {
    private Calculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new Calculator();
    }

    @Test
    @DisplayName("덧셈 연산을 수행한다")
    void add() {
        assertEquals(5, calculator.add(2, 3));
        assertEquals(-1, calculator.add(-4, 3));
        assertEquals(0, calculator.add(0, 0));
    }

    @Test
    @DisplayName("뺄셈 연산을 수행한다")
    void subtract() {
        assertEquals(3, calculator.subtract(5, 2));
    }

    @Test
    @DisplayName("곱셈 연산을 수행한다")
    void multiply() {
        assertEquals(6, calculator.multiply(2, 3));
        assertEquals(-6, calculator.multiply(2, -3));
        assertEquals(0, calculator.multiply(0, 5));
    }

    @Test
    @DisplayName("나눗셈 연산을 수행한다")
    void divide() {
        assertEquals(2, calculator.divide(6, 3));
        assertEquals(-2, calculator.divide(6, -3));
        assertEquals(0, calculator.divide(0, 5));
    }

    @Test
    @DisplayName("0으로 나누면 예외가 발생한다")
    void divideByZero() {
        assertThrows(IllegalArgumentException.class, () -> calculator.divide(10, 0));
    }
}
