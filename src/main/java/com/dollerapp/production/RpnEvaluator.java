package com.dollerapp.production;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Map;
import java.util.Stack;

/**
 * The central engine for evaluating RPN expressions using registered Operators.
 */
public class RpnEvaluator {

    /**
     * Evaluates a Reverse Polish Notation (RPN) expression.
     *
     * @param input    The RPN expression string.
     * @param mc       The MathContext for precision.
     * @param registry A map of supported operators.
     * @return The final result as a BigDecimal.
     */
    public static BigDecimal evaluate(String input, MathContext mc, Map<String, Operator> registry) {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException("Input expression cannot be null or empty.");
        }
        if (mc == null) {
            throw new IllegalArgumentException("MathContext cannot be null.");
        }
        if (registry == null) {
            throw new IllegalArgumentException("Operator registry cannot be null.");
        }

        String[] tokens = input.trim().split("\\s+");
        Stack<BigDecimal> stack = new Stack<>();

        for (String token : tokens) {
            Operator operator = registry.get(token);

            if (operator != null) {
                int requiredCount = operator.getOperandCount();

                // 1. Centralized Validation
                if (stack.size() < requiredCount) {
                    throw new IllegalArgumentException("Insufficient operands for operator: '" + token + "'");
                }

                // 2. Centralized Stack Extraction
                BigDecimal[] operands = new BigDecimal[requiredCount];
                // Pop in reverse order so operands[0] is the left-most operand
                for (int i = requiredCount - 1; i >= 0; i--) {
                    operands[i] = stack.pop();
                }

                // 3. Execution
                BigDecimal result = operator.calculate(operands, mc);
                stack.push(result);

            } else {
                // It's not a registered operator, so it must be a number
                try {
                    stack.push(new BigDecimal(token, mc));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid token in expression: '" + token + "'");
                }
            }
        }

        // Final validation
        if (stack.size() != 1) {
            throw new IllegalArgumentException("The expression has too many operands or missing operators.");
        }

        return stack.pop();
    }
}
