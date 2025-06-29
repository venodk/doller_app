package com.dollerapp.hiring.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class CalculatorTest {

    static Stream<Arguments> whenValidInput_thenReturnExpectedResult() {
        return Stream.of(
                arguments("3 4 +", 7.0),
                arguments("4 4 -", 0.0),
                arguments("4 -4 -", 8.0),
                arguments("3 4 *", 12.0),
                arguments("-3 4 *", -12.0),
                arguments("3 4 /", 0.75),
                arguments("3   4   +", 7.0),
                arguments("3 4 + 5 6 + *", 77.0),
                arguments("3 4 + 5 -6 + *", -7.0),
                arguments("3.71 4 + 5 6.0 + *", 84.81)
        );
    }

    @ParameterizedTest
    @MethodSource
    void whenValidInput_thenReturnExpectedResult(String expression, Double expectedResult) {
        assertEquals(expectedResult, Calculator.calculate(expression));
    }

    @ParameterizedTest
    @ValueSource(strings = { "null", "", "   ", "abc", "3L", "3 0 /"})
    void whenInValidInput_thenThrowException(String expression) {
        assertThrows(RuntimeException.class, () -> Calculator.calculate(expression));
    }
}
