package com.dollerapp.production;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides standard implementations of RPN operators and a registry factory.
 */
public class StandardOperators {

    /**
     * Builds and returns a registry containing all standard operators.
     */
    public static Map<String, Operator> getRegistry() {
        Map<String, Operator> registry = new HashMap<>();
        
        // Binary Operators
        registry.put("+", new Add());
        registry.put("-", new Subtract());
        registry.put("*", new Multiply());
        registry.put("/", new Divide());
        registry.put("%", new Modulo());
        registry.put("^", new Power());

        // Unary Operators
        registry.put("++", new Increment());
        registry.put("--", new Decrement());
        registry.put("neg", new Negate());
        registry.put("abs", new Absolute());
        registry.put("sqrt", new SquareRoot());
        
        return registry;
    }

    // --- Binary Operators (Arity 2) ---

    public static class Add implements Operator {
        @Override
        public int getOperandCount() { return 2; }
        @Override
        public BigDecimal calculate(BigDecimal[] args, MathContext mc) {
            return args[0].add(args[1], mc);
        }
    }

    public static class Subtract implements Operator {
        @Override
        public int getOperandCount() { return 2; }
        @Override
        public BigDecimal calculate(BigDecimal[] args, MathContext mc) {
            return args[0].subtract(args[1], mc);
        }
    }

    public static class Multiply implements Operator {
        @Override
        public int getOperandCount() { return 2; }
        @Override
        public BigDecimal calculate(BigDecimal[] args, MathContext mc) {
            return args[0].multiply(args[1], mc);
        }
    }

    public static class Divide implements Operator {
        @Override
        public int getOperandCount() { return 2; }
        @Override
        public BigDecimal calculate(BigDecimal[] args, MathContext mc) {
            if (args[1].compareTo(BigDecimal.ZERO) == 0) {
                throw new ArithmeticException("Division by zero is not allowed.");
            }
            return args[0].divide(args[1], mc);
        }
    }

    public static class Modulo implements Operator {
        @Override
        public int getOperandCount() { return 2; }
        @Override
        public BigDecimal calculate(BigDecimal[] args, MathContext mc) {
            if (args[1].compareTo(BigDecimal.ZERO) == 0) {
                throw new ArithmeticException("Modulo by zero is not allowed.");
            }
            return args[0].remainder(args[1], mc);
        }
    }

    public static class Power implements Operator {
        @Override
        public int getOperandCount() { return 2; }
        @Override
        public BigDecimal calculate(BigDecimal[] args, MathContext mc) {
            BigDecimal exponent = args[1];
            if (exponent.stripTrailingZeros().scale() > 0 || 
                exponent.compareTo(new BigDecimal(Integer.MAX_VALUE)) > 0 || 
                exponent.compareTo(new BigDecimal(Integer.MIN_VALUE)) < 0) {
                throw new IllegalArgumentException("Exponent must be an integer within int range for '^' operator.");
            }
            return args[0].pow(exponent.intValue(), mc);
        }
    }

    // --- Unary Operators (Arity 1) ---

    public static class Increment implements Operator {
        @Override
        public int getOperandCount() { return 1; }
        @Override
        public BigDecimal calculate(BigDecimal[] args, MathContext mc) {
            return args[0].add(BigDecimal.ONE, mc);
        }
    }

    public static class Decrement implements Operator {
        @Override
        public int getOperandCount() { return 1; }
        @Override
        public BigDecimal calculate(BigDecimal[] args, MathContext mc) {
            return args[0].subtract(BigDecimal.ONE, mc);
        }
    }

    public static class Negate implements Operator {
        @Override
        public int getOperandCount() { return 1; }
        @Override
        public BigDecimal calculate(BigDecimal[] args, MathContext mc) {
            return args[0].negate(mc);
        }
    }

    public static class Absolute implements Operator {
        @Override
        public int getOperandCount() { return 1; }
        @Override
        public BigDecimal calculate(BigDecimal[] args, MathContext mc) {
            return args[0].abs(mc);
        }
    }

    public static class SquareRoot implements Operator {
        @Override
        public int getOperandCount() { return 1; }
        @Override
        public BigDecimal calculate(BigDecimal[] args, MathContext mc) {
            BigDecimal value = args[0];
            if (value.compareTo(BigDecimal.ZERO) < 0) {
                throw new ArithmeticException("Square root of a negative number is not allowed.");
            }
            if (value.compareTo(BigDecimal.ZERO) == 0) {
                return BigDecimal.ZERO;
            }

            // Newton's method for BigDecimal square root
            BigDecimal x = new BigDecimal(Math.sqrt(value.doubleValue()), mc);
            BigDecimal fidelity = BigDecimal.ONE.divide(BigDecimal.TEN.pow(mc.getPrecision() + 1), mc);

            while (true) {
                BigDecimal fx = x.pow(2, mc).subtract(value, mc);
                BigDecimal fpx = x.multiply(new BigDecimal(2), mc);
                x = x.subtract(fx.divide(fpx, mc), mc);

                if (fx.abs(mc).compareTo(fidelity) < 0) {
                    break;
                }
            }
            return x.round(mc);
        }
    }
}
