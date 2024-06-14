package com.group12.husksheets.ui.utils;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;

/**
 * Utility class for spreadsheet operations
 */
public class SpreadsheetUtils {

    /**
     * Calculates the sum of the values in a range of cells
     *
     * @param tableView The TableView containing the spreadsheet data
     * @param range The range of cells (e.g., A1:B2)
     * @return The sum of the values
     */
    //Zach and Jason
    public static double sumRange(TableView<ObservableList<SimpleStringProperty>> tableView, String range) {
        String[] cells = range.split(":");
        String startCell = cells[0];
        String endCell = cells[1];
        int[] start = parseCellReference(startCell);
        int[] end = parseCellReference(endCell);
        double sum = 0;
        for (int row = start[0]; row <= end[0]; row++) {
            for (int col = start[1]; col <= end[1]; col++) {
                String cellValue = tableView.getItems().get(row).get(col).get();
                if (isNumeric(cellValue)) {
                    sum += Double.parseDouble(cellValue);
                }
            }
        }
        return sum;
    }

    /**
     * Calculates the sum of the value in a single cell
     *
     * @param tableView The TableView containing the spreadsheet data
     * @param cellRef The cell reference (e.g., A1)
     * @return The sum of the value
     */
    //Zach and Jason
    public static double sum(TableView<ObservableList<SimpleStringProperty>> tableView, String cellRef) {
        int[] cell = parseCellReference(cellRef);
        String cellValue = tableView.getItems().get(cell[0]).get(cell[1]).get();
        return isNumeric(cellValue) ? Double.parseDouble(cellValue) : 0;
    }

    /**
     * Calculates the minimum value in a range of cells
     *
     * @param tableView The TableView containing the spreadsheet data
     * @param range The range of cells (e.g., A1:B2)
     * @return The minimum value
     */
    //Zach and Jason
    public static double minRange(TableView<ObservableList<SimpleStringProperty>> tableView, String range) {
        String[] cells = range.split(":");
        String startCell = cells[0];
        String endCell = cells[1];
        int[] start = parseCellReference(startCell);
        int[] end = parseCellReference(endCell);
        double min = Double.MAX_VALUE;
        for (int row = start[0]; row <= end[0]; row++) {
            for (int col = start[1]; col <= end[1]; col++) {
                String cellValue = tableView.getItems().get(row).get(col).get();
                if (isNumeric(cellValue)) {
                    double value = Double.parseDouble(cellValue);
                    if (value < min) {
                        min = value;
                    }
                }
            }
        }
        return min;
    }

    /**
     * Calculates the minimum value in a single cell
     *
     * @param tableView The TableView containing the spreadsheet data
     * @param cellRef The cell reference (e.g., A1)
     * @return The minimum value
     */
    //Zach and Jason
    public static double min(TableView<ObservableList<SimpleStringProperty>> tableView, String cellRef) {
        int[] cell = parseCellReference(cellRef);
        String cellValue = tableView.getItems().get(cell[0]).get(cell[1]).get();
        return isNumeric(cellValue) ? Double.parseDouble(cellValue) : Double.MAX_VALUE;
    }

    /**
     * Calculates the maximum value in a range of cells
     *
     * @param tableView The TableView containing the spreadsheet data
     * @param range The range of cells (e.g., A1:B2)
     * @return The maximum value
     */
    //Zach and Jason
    public static double maxRange(TableView<ObservableList<SimpleStringProperty>> tableView, String range) {
        String[] cells = range.split(":");
        String startCell = cells[0];
        String endCell = cells[1];
        int[] start = parseCellReference(startCell);
        int[] end = parseCellReference(endCell);
        double max = Double.MIN_VALUE;
        for (int row = start[0]; row <= end[0]; row++) {
            for (int col = start[1]; col <= end[1]; col++) {
                String cellValue = tableView.getItems().get(row).get(col).get();
                if (isNumeric(cellValue)) {
                    double value = Double.parseDouble(cellValue);
                    if (value > max) {
                        max = value;
                    }
                }
            }
        }
        return max;
    }

    /**
     * Calculates the maximum value in a single cell
     *
     * @param tableView The TableView containing the spreadsheet data
     * @param cellRef The cell reference (e.g., A1)
     * @return The maximum value
     */
    //Zach and Jason
    public static double max(TableView<ObservableList<SimpleStringProperty>> tableView, String cellRef) {
        int[] cell = parseCellReference(cellRef);
        String cellValue = tableView.getItems().get(cell[0]).get(cell[1]).get();
        return isNumeric(cellValue) ? Double.parseDouble(cellValue) : Double.MIN_VALUE;
    }

    /**
     * Concatenates the values in a range of cells
     *
     * @param tableView The TableView containing the spreadsheet data
     * @param range The range of cells (e.g., A1:B2)
     * @return The concatenated result
     */
    //Zach and Jason
    public static String concatRange(TableView<ObservableList<SimpleStringProperty>> tableView, String range) {
        String[] cells = range.split(":");
        String startCell = cells[0];
        String endCell = cells[1];
        int[] start = parseCellReference(startCell);
        int[] end = parseCellReference(endCell);
        StringBuilder result = new StringBuilder();
        for (int row = start[0]; row <= end[0]; row++) {
            for (int col = start[1]; col <= end[1]; col++) {
                result.append(tableView.getItems().get(row).get(col).get());
            }
        }
        return result.toString();
    }

    /**
     * Concatenates the value in a single cell
     *
     * @param tableView The TableView containing the spreadsheet data
     * @param cellRef The cell reference (e.g., A1)
     * @return The concatenated result
     */
    //Zach and Jason
    public static String concat(TableView<ObservableList<SimpleStringProperty>> tableView, String cellRef) {
        int[] cell = parseCellReference(cellRef);
        return tableView.getItems().get(cell[0]).get(cell[1]).get();
    }

    /**
     * Finds the average value in a range of cells
     *
     * @param tableView The TableView containing the spreadsheet data
     * @param range The range of cells (e.g., A1:B2)
     * @return An array with the sum of the cell values and the count of cells
     */
    //Zach and Jason
    public static double[] avgRange(TableView<ObservableList<SimpleStringProperty>> tableView, String range) {
        String[] cells = range.split(":");
        int[] start = parseCellReference(cells[0]);
        int[] end = parseCellReference(cells[1]);

        double sum = 0;
        int count = 0;
        for (int row = start[0]; row <= end[0]; row++) {
            for (int col = start[1]; col <= end[1]; col++) {
                String cellValue = tableView.getItems().get(row).get(col).get();
                if (isNumeric(cellValue)) {
                    sum += Double.parseDouble(cellValue);
                    count++;
                }
            }
        }
        return new double[]{sum, count};
    }

    /**
     * Implements the IF function for conditional evaluation
     *
     * @param tableView The TableView containing the spreadsheet data
     * @param condition The condition to evaluate
     * @param trueValue The value if the condition is true
     * @param falseValue The value if the condition is false
     * @return The evaluated result
     */
    //Zach and Jason
    public static String ifFunction(TableView<ObservableList<SimpleStringProperty>> tableView, String condition, String trueValue, String falseValue) {
        boolean result = evaluateCondition(tableView, condition);
        return result ? evaluateExpression(tableView, trueValue) : evaluateExpression(tableView, falseValue);
    }

    /**
     * Debugging function for evaluating expressions
     *
     * @param tableView The TableView containing the spreadsheet data
     * @param expression The expression to evaluate
     * @return The evaluated result
     */
    //Zach and Jason
    public static String debugFunction(TableView<ObservableList<SimpleStringProperty>> tableView, String expression) {
        // For the purpose of debugging, simply evaluate the expression
        return evaluateExpression(tableView, expression);
    }

    /**
     * Evaluates a condition for the IF function
     *
     * @param tableView The TableView containing the spreadsheet data
     * @param condition The condition to evaluate
     * @return True if the condition is met, false otherwise
     */
    //Zach and Jason
    private static boolean evaluateCondition(TableView<ObservableList<SimpleStringProperty>> tableView, String condition) {
        // Implement basic comparison operators for the condition
        String[] parts;
        if (condition.contains("==")) {
            parts = condition.split("==");
            return evaluateExpression(tableView, parts[0]).equals(evaluateExpression(tableView, parts[1]));
        } else if (condition.contains("!=")) {
            parts = condition.split("!=");
            return !evaluateExpression(tableView, parts[0]).equals(evaluateExpression(tableView, parts[1]));
        } else if (condition.contains("<=")) {
            parts = condition.split("<=");
            return Double.parseDouble(evaluateExpression(tableView, parts[0])) <= Double.parseDouble(evaluateExpression(tableView, parts[1]));
        } else if (condition.contains(">=")) {
            parts = condition.split(">=");
            return Double.parseDouble(evaluateExpression(tableView, parts[0])) >= Double.parseDouble(evaluateExpression(tableView, parts[1]));
        } else if (condition.contains("<")) {
            parts = condition.split("<");
            return Double.parseDouble(evaluateExpression(tableView, parts[0])) < Double.parseDouble(evaluateExpression(tableView, parts[1]));
        } else if (condition.contains(">")) {
            parts = condition.split(">");
            return Double.parseDouble(evaluateExpression(tableView, parts[0])) > Double.parseDouble(evaluateExpression(tableView, parts[1]));
        }
        return false;
    }

    /**
     * Evaluates an expression for the IF function and debugging
     *
     * @param tableView The TableView containing the spreadsheet data
     * @param expression The expression to evaluate
     * @return The evaluated result
     */
    //Zach and Jason
    private static String evaluateExpression(TableView<ObservableList<SimpleStringProperty>> tableView, String expression) {
        // Implement evaluation logic for expressions
        expression = expression.trim();
        if (expression.startsWith("\"") && expression.endsWith("\"")) {
            return expression.substring(1, expression.length() - 1);
        } else if (isNumeric(expression)) {
            return expression;
        } else {
            int[] cell = parseCellReference(expression);
            return tableView.getItems().get(cell[0]).get(cell[1]).get();
        }
    }

    /**
     * Parses a cell reference into row and column indices
     *
     * @param cellRef The cell reference (e.g., A1)
     * @return An array with row and column indices
     */
    //Zach and Jason
    private static int[] parseCellReference(String cellRef) {
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
    //Zach and Jason
    private static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
