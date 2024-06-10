package com.group12.husksheets.models;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;

/**
 * Class to parse and evaluate spreadsheet formulas
 */
public class FormulaParser {
    private final TableView<ObservableList<SimpleStringProperty>> tableView; // The TableView containing the spreadsheet data

    /**
     * Constructs a FormulaParser object
     *
     * @param tableView The TableView to parse formulas for
     */
    public FormulaParser(TableView<ObservableList<SimpleStringProperty>> tableView) {
        this.tableView = tableView;
    }

    /**
     * Evaluates a formula
     *
     * @param formula The formula to evaluate
     * @return The result of the formula
     */
    public String evaluateFormula(String formula) {
        try {
            if (formula.startsWith("=")) {
                if (formula.startsWith("=SUM(") && formula.endsWith(")")) {
                    String cells = formula.substring(5, formula.length() - 1);
                    String[] cellRefs = cells.split(",");
                    double result = 0;
                    for (String cellRef : cellRefs) {
                        if (cellRef.contains(":")) {
                            result += SpreadsheetUtils.sumRange(tableView, cellRef);
                        } else {
                            result += SpreadsheetUtils.sum(tableView, cellRef);
                        }
                    }
                    return String.valueOf(result);
                }
                if (formula.startsWith("=MIN(") && formula.endsWith(")")) {
                    String cells = formula.substring(5, formula.length() - 1);
                    String[] cellRefs = cells.split(",");
                    double result = Double.MAX_VALUE;
                    for (String cellRef : cellRefs) {
                        if (cellRef.contains(":")) {
                            double minRange = SpreadsheetUtils.minRange(tableView, cellRef);
                            if (minRange < result) {
                                result = minRange;
                            }
                        } else {
                            double minValue = SpreadsheetUtils.min(tableView, cellRef);
                            if (minValue < result) {
                                result = minValue;
                            }
                        }
                    }
                    return String.valueOf(result);
                }
                if (formula.startsWith("=MAX(") && formula.endsWith(")")) {
                    String cells = formula.substring(5, formula.length() - 1);
                    String[] cellRefs = cells.split(",");
                    double result = Double.MIN_VALUE;
                    for (String cellRef : cellRefs) {
                        if (cellRef.contains(":")) {
                            double maxRange = SpreadsheetUtils.maxRange(tableView, cellRef);
                            if (maxRange > result) {
                                result = maxRange;
                            }
                        } else {
                            double maxValue = SpreadsheetUtils.max(tableView, cellRef);
                            if (maxValue > result) {
                                result = maxValue;
                            }
                        }
                    }
                    return String.valueOf(result);
                }
                if (formula.startsWith("=CONCAT(") && formula.endsWith(")")) {
                    String cells = formula.substring(8, formula.length() - 1);
                    String[] cellRefs = cells.split(",");
                    StringBuilder result = new StringBuilder();
                    for (String cellRef : cellRefs) {
                        if (cellRef.contains(":")) {
                            result.append(SpreadsheetUtils.concatRange(tableView, cellRef));
                        } else {
                            result.append(SpreadsheetUtils.concat(tableView, cellRef));
                        }
                    }
                    return result.toString();
                }
                if (formula.startsWith("=IF(") && formula.endsWith(")")) {
                    String[] parts = formula.substring(4, formula.length() - 1).split(",", 3);
                    if (parts.length == 3) {
                        return SpreadsheetUtils.ifFunction(tableView, parts[0].trim(), parts[1].trim(), parts[2].trim());
                    } else {
                        return "#REF!";
                    }
                }
                if (formula.startsWith("=DEBUG(") && formula.endsWith(")")) {
                    String expression = formula.substring(7, formula.length() - 1);
                    return SpreadsheetUtils.debugFunction(tableView, expression.trim());
                }
                // Evaluate arithmetic expressions using ArithmeticParser
                return String.valueOf(ArithmeticParser.evaluate(formula.substring(1)));
            }
        } catch (Exception e) {
            return "#REF!";
        }
        return formula;
    }

    /**
     * Parses a cell reference into row and column indices
     *
     * @param cellRef The cell reference (e.g., A1)
     * @return An array with row and column indices
     */
    public static int[] parseCellReference(String cellRef) {
        int colIndex = ColumnNameUtils.getColumnIndex(cellRef.replaceAll("[^A-Z]", ""));
        int rowIndex = Integer.parseInt(cellRef.replaceAll("[^0-9]", "")) - 1;
        return new int[]{rowIndex, colIndex};
    }

    /**
     * Checks if a string is numeric
     *
     * @param str The string to check
     * @return True if the string is numeric, false otherwise
     */
    public static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}