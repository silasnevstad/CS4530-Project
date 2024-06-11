package com.group12.husksheets.ui;

import com.group12.husksheets.ui.ArithmeticParser;

import java.util.ArrayList;
import java.util.List;


public class Expression {

    public static ArrayList<String> ops = new ArrayList<String>() {
        {
            add("+");
            add("-");
            add("*");
            add("/");
            add("<");
            add(">");
            add("=");
            add("<>");
            add("&");
            add("|");
            add(":");
        }
    };
    public static ArrayList<String> funs = new ArrayList<String>() {
        {
            add("IF");
            add("SUM");
            add("MIN");
            add("AVG");
            add("MAX");
            add("CONCAT");
            add("DEBUG");
        }
    };

    public static boolean isExpression(String str) {
        return isEOpE(str) || isExpInParens(str) || isFunExp(str) || isIntExp(str) || isStringExp(str) || isRefExp(str);
    }

    public static boolean isEOpE(String str) {

        String firstExp = "";
        String op = "";
        String secondExp = "";

        for(int i = 0; i < str.length(); i++) {
            if(ops.contains(str.substring(i, i + 1))) {
                if(ops.contains(str.substring(i, i + 2))) {
                    op = str.substring(i, i + 2);
                    secondExp = str.substring(i + 2);
                }
                else {
                    op = str.substring(i, i + 1);
                    secondExp = str.substring(i + 1);
                }
            }
            firstExp += str.substring(i, i + 1);
        }

        return isExpression(firstExp) && ops.contains(op) && isExpression(secondExp);

    }

    public static boolean isFunExp(String str) {
        for(int i = 2; i < str.length() && i < 6; i++) {
            if(funs.contains(str.substring(0, i))) {
                if(str.substring(i, i + 1) != "(") {
                    return false;
                }
                if(str.endsWith(")")) {
                    return false;
                }

                if(str.substring(i + 1, i + 2) != ")") {
                    String afterParen = str.substring(i + 1);
                    String insideParens = afterParen.substring(0, afterParen.length() - 1);

                    if(isExpression(insideParens)) {
                        return true;
                    }
                    else {
                        return isMultExps(insideParens);
                    }
                }
                else {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isMultExps(String str) {
        String[] tokens = str.split(" ");

        for(int i = 0; i < tokens.length - 1; i++) {
            if(!isExpression(tokens[i].substring(0, tokens[i].length() - 1))) {
                return false;
            }
        }
        return isExpression(tokens[tokens.length - 1]);
    }


    public static boolean isExpInParens(String str) {
        return str.startsWith("(") && str.endsWith(")") && isExpression(str.substring(1, str.length() - 1));
    }


    public static boolean isStringExp(String str) {
        return str.startsWith("\"") && str.endsWith("\"");
    }

    public static boolean isIntExp(String str) {
        try {
            Integer.parseInt(str.substring(1));
        } catch (NumberFormatException e) {
            return false;
        }
        return (str.startsWith("+") || str.startsWith("-"));
    }

    public static boolean isRefExp(String str) {
        return isValidRef(str);
    }

    public static String eOpEGetFirstExp(String str){
        String firstExp = "";

        for(int i = 0; i < str.length(); i++) {
            if(ops.contains(str.substring(i, i + 1))) {
                break;
            }
            firstExp += str.substring(i, i + 1);
        }

        return firstExp;
    }

    public static String eOpEGetOp(String str){
        String op = "";

        for(int i = 0; i < str.length(); i++) {
            if(ops.contains(str.substring(i, i + 1))) {
                if(ops.contains(str.substring(i, i + 2))) {
                    op = str.substring(i, i + 2);
                    break;
                }
                else {
                    op = str.substring(i, i + 1);
                    break;
                }
            }
        }

        return op;
    }

    public static String eOpEGetSecondExp(String str){
        String secondExp = "";

        for(int i = 0; i < str.length(); i++) {
            if(ops.contains(str.substring(i, i + 1))) {
                if(ops.contains(str.substring(i, i + 2))) {
                    secondExp = str.substring(i + 2);
                    break;
                }
                else {
                    secondExp = str.substring(i + 1);
                    break;
                }
            }
        }
        return secondExp;
    }

    public static boolean isValidRef(String str) {
        boolean dollarSignIdentified = false;
        boolean letterIdentified = false;
        boolean numberIdentified = false;

        for(int i = 0; i < str.length(); i++) {
            char currentChar = str.charAt(i);

            if(currentChar == '$') {
                if(letterIdentified | numberIdentified) {
                    return false;
                }
                else {
                    dollarSignIdentified = true;
                }
            }

            if(Character.isLetter(currentChar)) {
                if(!dollarSignIdentified || numberIdentified) {
                    return false;
                }
                else {
                    letterIdentified = true;
                }
            }

            if(Character.isDigit(currentChar)) {
                if(!dollarSignIdentified | !letterIdentified) {
                    return false;
                }
                else {
                    numberIdentified = true;
                }
            }
        }

        return dollarSignIdentified && letterIdentified && numberIdentified;
    }

    public static String evaluate(String str) {
        if(!isExpression(str)) {
            throw new IllegalArgumentException("Not a valid expression");
        }

        if(isEOpE(str)) {
            return ArithmeticParser.evaluate(evaluate(eOpEGetFirstExp(str)) + eOpEGetOp(str) + evaluate(eOpEGetSecondExp(str)));
        }
        else if(isFunExp(str)) {
            if(str.startsWith("IF(")) {
                String afterParen = str.substring(3);
                String insideParens = afterParen.substring(0, afterParen.length() - 1);

                String[] expressions = insideParens.split(", ");

                return evaluateIfFunction(expressions[0], expressions[1], expressions[2]);
            }

            if(str.startsWith("SUM(")) {
                String afterParen = str.substring(4);
                String insideParens = afterParen.substring(0, afterParen.length() - 1);

                return evaluateSumFunction(insideParens);
            }

            if(str.startsWith("MIN(")) {
                String afterParen = str.substring(4);
                String insideParens = afterParen.substring(0, afterParen.length() - 1);

                return evaluateMinFunction(insideParens);
            }

            if(str.startsWith("MAX(")) {
                String afterParen = str.substring(4);
                String insideParens = afterParen.substring(0, afterParen.length() - 1);

                return evaluateMaxFunction(insideParens);
            }

            if(str.startsWith("AVG(")) {
                String afterParen = str.substring(4);
                String insideParens = afterParen.substring(0, afterParen.length() - 1);

                return evaluateAvgFunction(insideParens);
            }

            if(str.startsWith("CONCAT(")) {
                String afterParen = str.substring(7);
                String insideParens = afterParen.substring(0, afterParen.length() - 1);

                return evaluateConcatFunction(insideParens);
            }

            if(str.startsWith("DEBUG(")) {
                String afterParen = str.substring(6);
                String insideParens = afterParen.substring(0, afterParen.length() - 1);

                return evaluateDebugFunction(insideParens);
            }

        }
        else if(isIntExp(str)) {
            return str;
        }
        else if(isStringExp(str)) {
            return str;
        }
        else if(isRefExp(str)) {
            return "TODO"; //ask the sheet to get the string in the cell denoted by the ref and call evaluate on it
        }
    }

    public static String evaluateIfFunction(String exp1, String exp2, String exp3) {
        if(!evaluate(exp1).matches("[0-9]+")) {
            throw new IllegalArgumentException("The first argument is not a number");
        }
        if(evaluate(exp1) == "0") {
            return evaluate(exp3);
        }
        else {
            return evaluate(exp2);
        }
    }

    public static ArrayList<String> listAllExpressions(String exp) {
        ArrayList<String> expressions = new ArrayList<>();

        String[] tokens = exp.split(" ");

        for(int i = 0; i < tokens.length - 1; i++) {
            if(isExpression(tokens[i].substring(0, tokens[i].length() - 1))) {
                expressions.add(tokens[i].substring(0, tokens[i].length() - 1));
            }
            else {
                throw new IllegalArgumentException("Arguments must be valid expressions");
            }
        }
        expressions.add(tokens[-1]);
        return expressions;
    }

    public static String evaluateSumFunction(String exp) {
        ArrayList<String> expressions = listAllExpressions(exp);

        int sum = 0;

        for(String expression : expressions) {
            int num = Integer.parseInt(evaluate(expression));

            sum += num;
        }

        return String.valueOf(sum);
    }

    public static String evaluateMinFunction(String exp) {
        ArrayList<String> expressions = listAllExpressions(exp);

        int min = Integer.parseInt(evaluate(expressions.get(0)));

        for(String expression : expressions) {
            int num = Integer.parseInt(evaluate(expression));

            if(num < min) {
                min = num;
            }
        }

        return String.valueOf(min);
    }

    public static String evaluateMaxFunction(String exp) {
        ArrayList<String> expressions = listAllExpressions(exp);

        int max = Integer.parseInt(evaluate(expressions.get(0)));

        for(String expression : expressions) {
            int num = Integer.parseInt(evaluate(expression));

            if(num > max) {
                max = num;
            }
        }

        return String.valueOf(max);
    }

    public static String evaluateAvgFunction(String exp) {
        ArrayList<String> expressions = listAllExpressions(exp);

        int sum = 0;

        for(String expression : expressions) {
            int num = Integer.parseInt(evaluate(expression));

            sum += num;
        }

        return String.valueOf(sum/expressions.size());
    }

    public static String evaluateConcatFunction(String exp) {
        ArrayList<String> expressions = listAllExpressions(exp);

        String accumulator = "";

        for(String expression : expressions) {
            String str = evaluate(expression);

            accumulator += str;
        }

        return accumulator;
    }

    public static String evaluateDebugFunction(String exp) {
        return evaluate(exp);
    }
}
