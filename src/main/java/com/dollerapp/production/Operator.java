package com.dollerapp.production;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Represents a mathematical operation in the RPN Calculator.
 */
public interface Operator {
    /**
     * @return The number of operands this operator requires (e.g., 1 for unary, 2 for binary).
     */
    int getOperandCount();

    /**
     * Executes the mathematical operation.
     *
     * @param operands An array of operands. The array size is guaranteed to match getOperandCount().
     *                 operands[0] is the left-most (oldest) operand on the stack.
     *                 operands[1] is the right-most (newest) operand.
     * @param mc       The MathContext to use for precision and rounding.
     * @return The result of the operation.
     * @throws ArithmeticException      for arithmetic errors (e.g., division by zero).
     * @throws IllegalArgumentException for invalid arguments.
     */
    BigDecimal calculate(BigDecimal[] operands, MathContext mc);
}
