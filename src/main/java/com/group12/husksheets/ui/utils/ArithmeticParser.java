// Owner: Zach Pulichino
package com.group12.husksheets.ui.utils;

import java.util.Stack;

public class ArithmeticParser {

  /**
   * Evaluates a given arithmetic expression.
   *
   * @param expression the arithmetic expression to evaluate
   * @return the result of the evaluated expression
   * @throws Exception if there is an error in the expression
   */
  public static double evaluate(String expression) throws Exception {
    // Convert the expression into a character array
    char[] tokens = expression.toCharArray();

    // Stack to store numerical values
    Stack<Double> values = new Stack<>();

    // Stack to store operators
    Stack<String> ops = new Stack<>();

    for (int i = 0; i < tokens.length; i++) {
      // Skip whitespaces
      if (tokens[i] == ' ')
        continue;

      // Current token is a number, parse the entire number
      if (tokens[i] >= '0' && tokens[i] <= '9' || tokens[i] == '.') {
        StringBuilder sbuf = new StringBuilder();
        while (i < tokens.length && (tokens[i] >= '0' && tokens[i] <= '9' || tokens[i] == '.'))
          sbuf.append(tokens[i++]);
        values.push(Double.parseDouble(sbuf.toString()));
        i--; // Adjust for the increment in the while loop
      }
      // Current token is an opening parenthesis, push it to ops stack
      else if (tokens[i] == '(') {
        ops.push(String.valueOf(tokens[i]));
      }
      // Closing parenthesis encountered, solve the entire sub-expression
      else if (tokens[i] == ')') {
        while (!ops.peek().equals("("))
          values.push(applyOp(ops.pop(), values.pop(), values.pop()));
        ops.pop(); // Pop the opening parenthesis
      }
      // Current token is an operator
      else if (isOperator(tokens[i])) {
        // While top of ops has same or greater precedence, apply the operator
        while (!ops.empty() && hasPrecedence(String.valueOf(tokens[i]), ops.peek()))
          values.push(applyOp(ops.pop(), values.pop(), values.pop()));
        ops.push(String.valueOf(tokens[i]));
      }
      // Check for the <> operator
      else if (tokens[i] == '<' && i + 1 < tokens.length && tokens[i + 1] == '>') {
        while (!ops.empty() && hasPrecedence("<>", ops.peek()))
          values.push(applyOp(ops.pop(), values.pop(), values.pop()));
        ops.push("<>");
        i++; // Skip the next character as it's part of <>
      }
      // Throw an exception for any invalid character
      else {
        throw new Exception("Invalid character in expression");
      }
    }

    // Apply remaining operators to remaining values
    while (!ops.empty())
      values.push(applyOp(ops.pop(), values.pop(), values.pop()));

    // The final value is the result of the expression
    return values.pop();
  }

  /**
   * Checks if a character is an operator.
   *
   * @param token the character to check
   * @return true if the character is an operator, false otherwise
   */
  private static boolean isOperator(char token) {
    return token == '+' || token == '-' || token == '*' || token == '/' || token == '<' || token == '>' || token == '=' || token == '&' || token == '|';
  }

  /**
   * Determines the precedence of operators.
   *
   * @param op1 the first operator
   * @param op2 the second operator
   * @return true if op2 has higher or same precedence as op1, false otherwise
   */
  private static boolean hasPrecedence(String op1, String op2) {
    if (op2.equals("(") || op2.equals(")"))
      return false;
    if ((op1.equals("*") || op1.equals("/")) && (op2.equals("+") || op2.equals("-")))
      return false;
    return true;
  }

  /**
   * Applies an operator to two operands.
   *
   * @param op the operator
   * @param b the second operand
   * @param a the first operand
   * @return the result of the operation
   * @throws Exception if the operator is invalid or division by zero occurs
   */
  private static double applyOp(String op, double b, double a) throws Exception {
    switch (op) {
      case "+":
        return a + b;
      case "-":
        return a - b;
      case "*":
        return a * b;
      case "/":
        if (b == 0)
          throw new UnsupportedOperationException("Cannot divide by zero");
        return a / b;
      case "<":
        return a < b ? 1 : 0;
      case ">":
        return a > b ? 1 : 0;
      case "=":
        return a == b ? 1 : 0;
      case "<>":
        return a != b ? 1 : 0;
      case "&":
        return (a != 0 && b != 0) ? 1 : 0;
      case "|":
        return (a != 0 || b != 0) ? 1 : 0;
      default:
        throw new Exception("Invalid operator");
    }
  }

  /**
   * Checks if a string is an arithmetic expression
   *
   * @param expression the string to check
   * @return true if the string is an arithmetic expression, false otherwise
   */
  public static boolean isArithmeticExpression(String expression) {
    return expression.matches(".*[0-9+\\-*/().].*");
  }
}
