package com.dollerapp.hiring.util;

import java.util.Stack;

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
}
