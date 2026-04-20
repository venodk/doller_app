package com.dollerapp.hiring.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class CalculatorTest {

    // Existing tests for the 'calculate' method (unchanged)
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
                arguments("3.71 4 + 5 6.0 + *", 84.81),
                arguments("0.1 0.2 +", 0.3),
                arguments("1 3 /", 0.3333333333),
                arguments("2 3 /", 0.6666666667),
                arguments("-1 +3 /", -0.33333333),
                arguments("1.03  0.42 -", 0.61)

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

    // New tests for 'calculateProductionReady' method
    static Stream<Arguments> validExpressionsProductionReady() {
        return Stream.of(
                arguments("3 4 +", new BigDecimal("7")),
                arguments("10 5 -", new BigDecimal("5")),
                arguments("5 10 -", new BigDecimal("-5")),
                arguments("3 4 *", new BigDecimal("12")),
                arguments("10 4 /", new BigDecimal("2.5")),
                arguments("3 4 + 5 *", new BigDecimal("35")),
                arguments("3 4 5 * +", new BigDecimal("23")),
                arguments("0.1 0.2 +", new BigDecimal("0.3")),
                arguments("1.03 0.42 -", new BigDecimal("0.61")),
                arguments("1 3 /", new BigDecimal("0.3333333333")), // 1/3 with SCALE 10
                arguments("2 3 /", new BigDecimal("0.6666666667")), // 2/3 with SCALE 10
                arguments("10 3 /", new BigDecimal("3.3333333333")), // 10/3 with SCALE 10
                arguments("5 2 3 + *", new BigDecimal("25")),
                arguments("5 2 + 3 *", new BigDecimal("21")),
                arguments("5 2 - 3 *", new BigDecimal("9")),
                arguments("5 2 / 3 *", new BigDecimal("7.5")),
                arguments("1 2 3 4 5 + + + +", new BigDecimal("15")),
                arguments("1 2 + 3 + 4 + 5 +", new BigDecimal("15")),
                arguments("0.0000000001 0.0000000002 +", new BigDecimal("0.0000000003")),
                arguments("1.2345678901 1.0000000000 *", new BigDecimal("1.2345678901"))
        );
    }

    @ParameterizedTest
    @MethodSource("validExpressionsProductionReady")
    void whenValidInputProductionReady_thenReturnCorrectResult(String expression, BigDecimal expected) {
        BigDecimal actual = Calculator.calculateProductionReady(expression);
        assertEquals(0, expected.compareTo(actual), "Expected " + expected + " but got " + actual);
    }

    @Test
    void whenInputIsNullProductionReady_thenThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> Calculator.calculateProductionReady(null));
    }

    @Test
    void whenInputIsEmptyProductionReady_thenThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> Calculator.calculateProductionReady(""));
    }

    @Test
    void whenInputIsBlankProductionReady_thenThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> Calculator.calculateProductionReady("   "));
    }

    @ParameterizedTest
    @ValueSource(strings = {"+", "3 +", "3 4 5 +", "3 4 + 5"})
    void whenInsufficientOperandsProductionReady_thenThrowIllegalArgumentException(String expression) {
        assertThrows(IllegalArgumentException.class, () -> Calculator.calculateProductionReady(expression));
    }

    @Test
    void whenDivisionByZeroProductionReady_thenThrowArithmeticException() {
        assertThrows(ArithmeticException.class, () -> Calculator.calculateProductionReady("3 0 /"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"a b +", "3 4 ++", "3 4 . +"})
    void whenInvalidTokenProductionReady_thenThrowIllegalArgumentException(String expression) {
        assertThrows(IllegalArgumentException.class, () -> Calculator.calculateProductionReady(expression));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1 2 3", "1 2 3 +"})
    void whenTooManyOperandsProductionReady_thenThrowIllegalArgumentException(String expression) {
        var message = assertThrows(IllegalArgumentException.class, () -> Calculator.calculateProductionReady(expression));
        assertEquals("The expression has too many operands.", message.getMessage());
    }

    // --- Tests for calculateExtendedRpn method ---
    private static final MathContext MC = new MathContext(10, RoundingMode.HALF_UP);

    static Stream<Arguments> validExpressionsExtendedRpn() {
        return Stream.of(
                // Basic operations
                arguments("3 4 +", new BigDecimal("7"), MC),
                arguments("10 5 -", new BigDecimal("5"), MC),
                arguments("3 4 *", new BigDecimal("12"), MC),
                arguments("10 4 /", new BigDecimal("2.5"), MC),
                arguments("1 3 /", new BigDecimal("0.3333333333"), MC), // 1/3 with MC precision

                // Exponentiation (^)
                arguments("2 3 ^", new BigDecimal("8"), MC),
                arguments("5 0 ^", new BigDecimal("1"), MC),
                arguments("4 0.5 ^", new BigDecimal("2"), MC), // sqrt(4)
                arguments("2 -1 ^", new BigDecimal("0.5"), MC), // 1/2

                // Modulo (%)
                arguments("7 3 %", new BigDecimal("1"), MC),
                arguments("10 3 %", new BigDecimal("1"), MC),
                arguments("10.5 3 %", new BigDecimal("1.5"), MC),

                // Unary Minus (neg)
                arguments("5 neg", new BigDecimal("-5"), MC),
                arguments("-5 neg", new BigDecimal("5"), MC),
                arguments("0 neg", new BigDecimal("0"), MC),

                // Absolute Value (abs)
                arguments("-10 abs", new BigDecimal("10"), MC),
                arguments("10 abs", new BigDecimal("10"), MC),
                arguments("0 abs", new BigDecimal("0"), MC),

                // Square Root (sqrt)
                arguments("9 sqrt", new BigDecimal("3"), MC),
                arguments("25 sqrt", new BigDecimal("5"), MC),
                arguments("2 sqrt", new BigDecimal("1.414213562"), MC), // sqrt(2) with MC precision
                arguments("0 sqrt", new BigDecimal("0"), MC),

                // Combined operations
                arguments("2 3 ^ 5 +", new BigDecimal("13"), MC), // (2^3) + 5 = 8 + 5 = 13
                arguments("10 neg abs", new BigDecimal("10"), MC), // abs(-10) = 10
                arguments("16 sqrt 2 *", new BigDecimal("8"), MC), // sqrt(16) * 2 = 4 * 2 = 8
                arguments("5 2 ^ 3 %", new BigDecimal("1"), MC) // (5^2) % 3 = 25 % 3 = 1
        );
    }

    @ParameterizedTest
    @MethodSource("validExpressionsExtendedRpn")
    void whenValidInputExtendedRpn_thenReturnCorrectResult(String expression, BigDecimal expected, MathContext mc) {
        BigDecimal actual = Calculator.calculateExtendedRpn(expression, mc);
        assertEquals(0, expected.compareTo(actual), "Expected " + expected + " but got " + actual);
    }

    @Test
    void whenInputIsNullExtendedRpn_thenThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> Calculator.calculateExtendedRpn(null, MC));
    }

    @Test
    void whenInputIsEmptyExtendedRpn_thenThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> Calculator.calculateExtendedRpn("", MC));
    }

    @Test
    void whenInputIsBlankExtendedRpn_thenThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> Calculator.calculateExtendedRpn("   ", MC));
    }

    @Test
    void whenMathContextIsNullExtendedRpn_thenThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> Calculator.calculateExtendedRpn("1 2 +", null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"+", "3 +", "3 4 5 +", "3 4 + 5", "neg", "sqrt"})
    void whenInsufficientOperandsExtendedRpn_thenThrowIllegalArgumentException(String expression) {
        assertThrows(IllegalArgumentException.class, () -> Calculator.calculateExtendedRpn(expression, MC));
    }

    @Test
    void whenDivisionByZeroExtendedRpn_thenThrowArithmeticException() {
        assertThrows(ArithmeticException.class, () -> Calculator.calculateExtendedRpn("3 0 /", MC));
    }

    @Test
    void whenModuloByZeroExtendedRpn_thenThrowArithmeticException() {
        assertThrows(ArithmeticException.class, () -> Calculator.calculateExtendedRpn("3 0 %", MC));
    }

    @Test
    void whenSqrtOfNegativeExtendedRpn_thenThrowArithmeticException() {
        assertThrows(ArithmeticException.class, () -> Calculator.calculateExtendedRpn("-9 sqrt", MC));
    }

    @ParameterizedTest
    @ValueSource(strings = {"2 0.5 ^", "2 1.5 ^"}) // Non-integer exponents
    void whenNonIntegerExponentExtendedRpn_thenThrowIllegalArgumentException(String expression) {
        assertThrows(IllegalArgumentException.class, () -> Calculator.calculateExtendedRpn(expression, MC));
    }

    @ParameterizedTest
    @ValueSource(strings = {"a b +", "3 4 ++", "3 4 . +", "invalid"})
    void whenInvalidTokenExtendedRpn_thenThrowIllegalArgumentException(String expression) {
        assertThrows(IllegalArgumentException.class, () -> Calculator.calculateExtendedRpn(expression, MC));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1 2 3", "1 2 3 +"})
    void whenTooManyOperandsExtendedRpn_thenThrowIllegalArgumentException(String expression) {
        var message = assertThrows(IllegalArgumentException.class, () -> Calculator.calculateExtendedRpn(expression, MC));
        assertEquals("The expression has too many operands or too many operators.", message.getMessage());
    }
}
