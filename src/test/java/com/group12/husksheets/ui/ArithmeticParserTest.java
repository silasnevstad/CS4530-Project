// Owner: Zach Pulichino
package com.group12.husksheets.ui;

import com.group12.husksheets.ui.utils.ArithmeticParser;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ArithmeticParserTest {
  // Test evaluating a simple addition expression
  @Test
  void testEvaluateSimpleAddition() throws Exception {
    String expression = "2+3";
    double result = ArithmeticParser.evaluate(expression);
    assertEquals(5.0, result, 0.0001);
  }

  // Test evaluating a simple subtraction expression
  @Test
  void testEvaluateSimpleSubtraction() throws Exception {
    String expression = "5-4";
    double result = ArithmeticParser.evaluate(expression);
    assertEquals(1.0, result, 0.0001);
  }

  // Test evaluating a simple multiplication expression
  @Test
  void testEvaluateSimpleMultiplication() throws Exception {
    String expression = "2*3";
    double result = ArithmeticParser.evaluate(expression);
    assertEquals(6.0, result, 0.0001);
  }

  // Test evaluating a simple division expression
  @Test
  void testEvaluateSimpleDivision() throws Exception {
    String expression = "6/3";
    double result = ArithmeticParser.evaluate(expression);
    assertEquals(2.0, result, 0.0001);
  }

  // Test evaluating an expression with parentheses
  @Test
  void testEvaluateWithParentheses() throws Exception {
    String expression = "(2+3)*1+2";
    double result = ArithmeticParser.evaluate(expression);
    assertEquals(7.0, result, 0.0001);
  }

  // Test evaluating a complex expression
  @Test
  void testEvaluateComplexExpression() throws Exception {
    String expression = "2+3*4";
    double result = ArithmeticParser.evaluate(expression);
    assertEquals(14.0, result, 0.0001);
  }

  // Test evaluating an expression with spaces
  @Test
  void testEvaluateExpressionWithSpaces() throws Exception {
    String expression = " 2 + 3 * 1 + 2 ";
    double result = ArithmeticParser.evaluate(expression);
    assertEquals(7.0, result, 0.0001);
  }

  // Test evaluating an expression with decimal numbers
  @Test
  void testEvaluateExpressionWithDecimalNumbers() throws Exception {
    String expression = "2.5+5.0";
    double result = ArithmeticParser.evaluate(expression);
    assertEquals(7.5, result, 0.0001);
  }

  // Test evaluating an expression with division by zero
  @Test
  void testEvaluateDivisionByZero() {
    String expression = "5/0";
    Exception exception = assertThrows(Exception.class, () -> ArithmeticParser.evaluate(expression));
    assertEquals("Cannot divide by zero", exception.getMessage());
  }

  // Test evaluating an expression with an invalid character
  @Test
  void testEvaluateInvalidCharacter() {
    String expression = "5+3a";
    Exception exception = assertThrows(Exception.class, () -> ArithmeticParser.evaluate(expression));
    assertEquals("Invalid character in expression", exception.getMessage());
  }

  // Test evaluating an expression with the less than operator
  @Test
  void testEvaluateLessThanOperator() throws Exception {
    String expression1 = "3<5";
    String expression2 = "5<3";
    double result1 = ArithmeticParser.evaluate(expression1);
    double result2 = ArithmeticParser.evaluate(expression2);
    assertEquals(1.0, result1, 0.0001);
    assertEquals(0.0, result2, 0.0001);
  }

  // Test evaluating an expression with the greater than operator
  @Test
  void testEvaluateGreaterThanOperator() throws Exception {
    String expression1 = "5>3";
    String expression2 = "3>5";
    double result1 = ArithmeticParser.evaluate(expression1);
    double result2 = ArithmeticParser.evaluate(expression2);
    assertEquals(1.0, result1, 0.0001);
    assertEquals(0.0, result2, 0.0001);
  }

  // Test evaluating an expression with the equality operator
  @Test
  void testEvaluateEqualityOperator() throws Exception {
    String expression1 = "5=5";
    String expression2 = "5=3";
    double result1 = ArithmeticParser.evaluate(expression1);
    double result2 = ArithmeticParser.evaluate(expression2);
    assertEquals(1.0, result1, 0.0001);
    assertEquals(0.0, result2, 0.0001);
  }

  // Test evaluating an expression with the AND operator
  @Test
  void testEvaluateAndOperator() throws Exception {
    String expression1 = "1&1";
    String expression2 = "1&0";
    double result1 = ArithmeticParser.evaluate(expression1);
    double result2 = ArithmeticParser.evaluate(expression2);
    assertEquals(1.0, result1, 0.0001);
    assertEquals(0.0, result2, 0.0001);
  }

  // Test evaluating an expression with the OR operator
  @Test
  void testEvaluateOrOperator() throws Exception {
    String expression1 = "1|0";
    String expression2 = "0|0";
    double result1 = ArithmeticParser.evaluate(expression1);
    double result2 = ArithmeticParser.evaluate(expression2);
    assertEquals(1.0, result1, 0.0001);
    assertEquals(0.0, result2, 0.0001);
  }

  // Test if a string is an arithmetic expression
  @Test
  void testIsArithmeticExpression() {
    String expression1 = "2+3";
    String expression2 = "5-4";
    String expression3 = "abc";
    String expression4 = "5*6/2";
    String expression5 = "";
    assertTrue(ArithmeticParser.isArithmeticExpression(expression1));
    assertTrue(ArithmeticParser.isArithmeticExpression(expression2));
    assertFalse(ArithmeticParser.isArithmeticExpression(expression3));
    assertTrue(ArithmeticParser.isArithmeticExpression(expression4));
    assertFalse(ArithmeticParser.isArithmeticExpression(expression5));
  }

  // Test evaluating an expression with multiple operators
  @Test
  void testEvaluateExpressionWithMultipleOperators() throws Exception {
    String expression = "2+3*4/6-1";
    double result = ArithmeticParser.evaluate(expression);
    assertEquals(3.0, result, 0.0001);
  }

  // Test evaluating an expression with nested parentheses
  @Test
  void testEvaluateNestedParentheses() throws Exception {
    String expression = "2*(3+(4*(5-3)))";
    double result = ArithmeticParser.evaluate(expression);
    assertEquals(22.0, result, 0.0001);
  }

  // Test evaluating an expression starting with parentheses
  @Test
  void testEvaluateExpressionStartingWithParentheses() throws Exception {
    String expression = "(2+3)*2+4";
    double result = ArithmeticParser.evaluate(expression);
    assertEquals(14.0, result, 0.0001);
  }
}