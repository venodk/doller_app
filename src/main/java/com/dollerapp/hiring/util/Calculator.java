package com.dollerapp.hiring.util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Stack;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Calculator {

    public static Double calculate(String input) {
        if (input == null) {
            throw new RuntimeException("noll not expected");
        }

        String[] tokens = input.split("\\s+");
        Stack<Double> stack = new Stack<>();
        Double num1;
        Double num2;

        for (var token : tokens) {
            switch (token) {
                case "+" :
                    if (stack.size() < 2) {
                        throw new RuntimeException("Invalid operator: " + token);
                    }
                    double  sum = stack.pop() + stack.pop();
                    stack.push(sum);
                    break;
                case "-" :
                    if (stack.size() < 2) {
                        throw new RuntimeException("Invalid operator: " + token);
                    }
                    num2 = stack.pop();
                    num1 = stack.pop();
                    double diff = num1 - num2;
                    stack.push(diff);
                    break;
                case "*" :
                    if (stack.size() < 2) {
                        throw new RuntimeException("Invalid operator: " + token);
                    }
                    double product = stack.pop() * stack.pop();
                    stack.push(product);
                    break;
                case "/" :
                    if (stack.size() < 2) {
                        throw new RuntimeException("Invalid operator: " + token);
                    }
                    if (stack.peek().equals(0.0)) {
                        throw new RuntimeException("Division by Zero.");
                    }
                    num2 = stack.pop();
                    num1 = stack.pop();
                    double division = num1 / num2 ;
                    stack.push(division);
                    break;
                default:
                    stack.push(Double.parseDouble(token));
                    break;
            }
        }
        if (stack.size() != 1) {
            throw new RuntimeException("Invalid input expression");
        }
        return stack.peek();
    }

    public static BigDecimal calculateProductionReady(String input) {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException("Input expression cannot be null or empty.");
        }

        String[] tokens = input.trim().split("\\s+");
        Stack<BigDecimal> stack = new Stack<>();
        final int SCALE = 10; // Define a scale for division

        for (String token : tokens) {
            switch (token) {
                case "+":
                    if (stack.size() < 2) {
                        throw new IllegalArgumentException("Insufficient operands for addition.");
                    }
                    BigDecimal addend1 = stack.pop();
                    BigDecimal addend2 = stack.pop();
                    stack.push(addend2.add(addend1));
                    break;
                case "-":
                    if (stack.size() < 2) {
                        throw new IllegalArgumentException("Insufficient operands for subtraction.");
                    }
                    BigDecimal subtrahend = stack.pop();
                    BigDecimal minuend = stack.pop();
                    stack.push(minuend.subtract(subtrahend));
                    break;
                case "*":
                    if (stack.size() < 2) {
                        throw new IllegalArgumentException("Insufficient operands for multiplication.");
                    }
                    BigDecimal factor1 = stack.pop();
                    BigDecimal factor2 = stack.pop();
                    stack.push(factor2.multiply(factor1));
                    break;
                case "/":
                    if (stack.size() < 2) {
                        throw new IllegalArgumentException("Insufficient operands for division.");
                    }
                    BigDecimal divisor = stack.pop();
                    if (divisor.compareTo(BigDecimal.ZERO) == 0) {
                        throw new ArithmeticException("Division by zero is not allowed.");
                    }
                    BigDecimal dividend = stack.pop();
                    stack.push(dividend.divide(divisor, SCALE, RoundingMode.HALF_UP));
                    break;
                default:
                    try {
                        stack.push(new BigDecimal(token));
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Invalid token in expression: " + token);
                    }
                    break;
            }
        }

        if (stack.size() != 1) {
            throw new IllegalArgumentException("The expression has too many operands.");
        }

        return stack.pop();
    }

    public static BigDecimal calculateExtendedRpn(String input, MathContext mc) {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException("Input expression cannot be null or empty.");
        }
        if (mc == null) {
            throw new IllegalArgumentException("MathContext cannot be null.");
        }

        String[] tokens = input.trim().split("\\s+");
        Stack<BigDecimal> stack = new Stack<>();

        for (String token : tokens) {
            switch (token) {
                case "+":
                    performBinaryOp(stack, (op1, op2) -> op2.add(op1, mc), "addition");
                    break;
                case "-":
                    performBinaryOp(stack, (op1, op2) -> op2.subtract(op1, mc), "subtraction");
                    break;
                case "*":
                    performBinaryOp(stack, (op1, op2) -> op2.multiply(op1, mc), "multiplication");
                    break;
                case "/":
                    performBinaryOp(stack, (op1, op2) -> {
                        if (op1.compareTo(BigDecimal.ZERO) == 0) {
                            throw new ArithmeticException("Division by zero is not allowed.");
                        }
                        return op2.divide(op1, mc);
                    }, "division");
                    break;
                case "^": // Exponentiation
                    performBinaryOp(stack, (op1, op2) -> {
                        // BigDecimal.pow() takes an int, so we need to handle non-integer exponents
                        if (op1.stripTrailingZeros().scale() > 0 || op1.compareTo(new BigDecimal(Integer.MAX_VALUE)) > 0 || op1.compareTo(new BigDecimal(Integer.MIN_VALUE)) < 0) {
                            throw new IllegalArgumentException("Exponent must be an integer within int range for '^' operator.");
                        }
                        return op2.pow(op1.intValue(), mc);
                    }, "exponentiation");
                    break;
                case "%": // Modulo
                    performBinaryOp(stack, (op1, op2) -> {
                        if (op1.compareTo(BigDecimal.ZERO) == 0) {
                            throw new ArithmeticException("Modulo by zero is not allowed.");
                        }
                        return op2.remainder(op1, mc);
                    }, "modulo");
                    break;
                case "neg": // Unary Minus
                    performUnaryOp(stack, op -> op.negate(mc), "negation");
                    break;
                case "abs": // Absolute Value
                    performUnaryOp(stack, op -> op.abs(mc), "absolute value");
                    break;
                case "sqrt": // Square Root
                    performUnaryOp(stack, op -> {
                        if (op.compareTo(BigDecimal.ZERO) < 0) {
                            throw new ArithmeticException("Square root of a negative number is not allowed.");
                        }
                        return sqrt(op, mc);
                    }, "square root");
                    break;
                default:
                    try {
                        stack.push(new BigDecimal(token, mc));
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Invalid token in expression: " + token);
                    }
                    break;
            }
        }

        if (stack.size() != 1) {
            throw new IllegalArgumentException("The expression has too many operands or too many operators.");
        }

        return stack.pop();
    }

    private static void performBinaryOp(Stack<BigDecimal> stack, BiFunction<BigDecimal, BigDecimal, BigDecimal> operation, String opName) {
        if (stack.size() < 2) {
            throw new IllegalArgumentException("Insufficient operands for " + opName + ".");
        }
        BigDecimal op1 = stack.pop();
        BigDecimal op2 = stack.pop();
        stack.push(operation.apply(op1, op2));
    }

    private static void performUnaryOp(Stack<BigDecimal> stack, Function<BigDecimal, BigDecimal> operation, String opName) {
        if (stack.isEmpty()) {
            throw new IllegalArgumentException("Insufficient operands for " + opName + ".");
        }
        BigDecimal op = stack.pop();
        stack.push(operation.apply(op));
    }

    /**
     * Computes the square root of a BigDecimal using Newton's method.
     *
     * @param value The value to compute the square root of.
     * @param mc The MathContext to use for precision and rounding.
     * @return The square root of the value.
     * @throws ArithmeticException if the value is negative.
     */
    private static BigDecimal sqrt(BigDecimal value, MathContext mc) {
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new ArithmeticException("Square root of a negative number is not allowed.");
        }
        if (value.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal x = new BigDecimal(Math.sqrt(value.doubleValue()), mc); // Initial guess
        BigDecimal fidelity = BigDecimal.ONE.divide(new BigDecimal(10).pow(mc.getPrecision() + 1), mc); // How close we need to be

        while (true) {
            BigDecimal fx = x.pow(2, mc).subtract(value, mc);
            BigDecimal fpx = x.multiply(new BigDecimal(2), mc);
            x = x.subtract(fx.divide(fpx, mc), mc);

            if (fx.abs(mc).compareTo(fidelity) < 0) { // Check if the change is small enough
                break;
            }
        }
        return x.round(mc);
    }
}
