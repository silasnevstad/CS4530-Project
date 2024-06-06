package com.example.husksheets_jktesting;

import javafx.collections.ObservableList;
import javafx.scene.control.TableView;

/**
 * Utility class for spreadsheet functions
 */
public class SpreadsheetUtils {
    /**
     * Converts a column name (e.g., "A", "AA") to a column index.
     *
     * @param columnName The column name
     * @return The column index
     */
    private static int getColumnIndex(String columnName) {
        int columnIndex = 0;
        for (int i = 0; i < columnName.length(); i++) {
            columnIndex = columnIndex * 26 + (columnName.charAt(i) - 'A' + 1);
        }
        return columnIndex - 1;
    }

    /**
     * Gets the numeric value of a cell.
     *
     * @param tableView The TableView containing the cell
     * @param cellRef   The cell reference (e.g., "A1")
     * @return The numeric value of the cell
     */
    public static double getCellValue(TableView<ObservableList<String>> tableView, String cellRef) {
        int row = Integer.parseInt(cellRef.replaceAll("[^0-9]", "")) - 1;
        String colString = cellRef.replaceAll("[0-9]", "");
        int col = getColumnIndex(colString);

        String cellValue = tableView.getItems().get(row).get(col);

        // Strip formatting markers
        String strippedValue = stripMarkers(cellValue);

        if (strippedValue.matches("-?\\d+(\\.\\d+)?")) {
            return Double.parseDouble(strippedValue);
        }
        return 0.0; // Return 0 if the cell value is not a number
    }

    /**
     * Gets the text value of a cell.
     *
     * @param tableView The TableView containing the cell
     * @param cellRef   The cell reference (e.g., "A1")
     * @return The text value of the cell
     */
    public static String getCellText(TableView<ObservableList<String>> tableView, String cellRef) {
        int row = Integer.parseInt(cellRef.replaceAll("[^0-9]", "")) - 1;
        String colString = cellRef.replaceAll("[0-9]", "");
        int col = getColumnIndex(colString);

        String cellValue = tableView.getItems().get(row).get(col);

        // Strip formatting markers
        return stripMarkers(cellValue);
    }

    /**
     * Sums the values of multiple cells.
     *
     * @param tableView The TableView containing the cells
     * @param cellRefs  The cell references to sum
     * @return The sum of the cell values
     */
    public static double sum(TableView<ObservableList<String>> tableView, String... cellRefs) {
        double total = 0;
        for (String cellRef : cellRefs) {
            total += getCellValue(tableView, cellRef);
        }
        return total;
    }

    /**
     * Sums the values of a range of cells.
     *
     * @param tableView The TableView containing the cells
     * @param range     The range of cells to sum (e.g., "A1:A10")
     * @return The sum of the cell values
     */
    public static double sumRange(TableView<ObservableList<String>> tableView, String range) {
        String[] parts = range.split(":");
        if (parts.length != 2) {
            return 0;
        }
        String start = parts[0];
        String end = parts[1];

        int startRow = Integer.parseInt(start.replaceAll("[^0-9]", "")) - 1;
        int endRow = Integer.parseInt(end.replaceAll("[^0-9]", "")) - 1;
        int startCol = getColumnIndex(start.replaceAll("[0-9]", ""));
        int endCol = getColumnIndex(end.replaceAll("[0-9]", ""));

        double total = 0;
        for (int row = startRow; row <= endRow; row++) {
            for (int col = startCol; col <= endCol; col++) {
                total += getCellValue(tableView, ColumnNameUtils.getColumnName(col) + (row + 1));
            }
        }
        return total;
    }

    /**
     * Finds the minimum value of multiple cells.
     *
     * @param tableView The TableView containing the cells
     * @param cellRefs  The cell references to find the minimum of
     * @return The minimum cell value
     */
    public static double min(TableView<ObservableList<String>> tableView, String... cellRefs) {
        double minVal = Double.MAX_VALUE;
        for (String cellRef : cellRefs) {
            double cellValue = getCellValue(tableView, cellRef);
            if (cellValue < minVal) {
                minVal = cellValue;
            }
        }
        return minVal;
    }

    /**
     * Finds the minimum value of a range of cells.
     *
     * @param tableView The TableView containing the cells
     * @param range     The range of cells to find the minimum of (e.g., "A1:A10")
     * @return The minimum cell value
     */
    public static double minRange(TableView<ObservableList<String>> tableView, String range) {
        String[] parts = range.split(":");
        if (parts.length != 2) {
            return Double.MAX_VALUE;
        }
        String start = parts[0];
        String end = parts[1];

        int startRow = Integer.parseInt(start.replaceAll("[^0-9]", "")) - 1;
        int endRow = Integer.parseInt(end.replaceAll("[^0-9]", "")) - 1;
        int startCol = getColumnIndex(start.replaceAll("[0-9]", ""));
        int endCol = getColumnIndex(end.replaceAll("[0-9]", ""));

        double minVal = Double.MAX_VALUE;
        for (int row = startRow; row <= endRow; row++) {
            for (int col = startCol; col <= endCol; col++) {
                double cellValue = getCellValue(tableView, ColumnNameUtils.getColumnName(col) + (row + 1));
                if (cellValue < minVal) {
                    minVal = cellValue;
                }
            }
        }
        return minVal;
    }

    /**
     * Finds the maximum value of multiple cells.
     *
     * @param tableView The TableView containing the cells
     * @param cellRefs  The cell references to find the maximum of
     * @return The maximum cell value
     */
    public static double max(TableView<ObservableList<String>> tableView, String... cellRefs) {
        double maxVal = Double.MIN_VALUE;
        for (String cellRef : cellRefs) {
            double cellValue = getCellValue(tableView, cellRef);
            if (cellValue > maxVal) {
                maxVal = cellValue;
            }
        }
        return maxVal;
    }

    /**
     * Finds the maximum value of a range of cells.
     *
     * @param tableView The TableView containing the cells
     * @param range     The range of cells to find the maximum of (e.g., "A1:A10")
     * @return The maximum cell value
     */
    public static double maxRange(TableView<ObservableList<String>> tableView, String range) {
        String[] parts = range.split(":");
        if (parts.length != 2) {
            return Double.MIN_VALUE;
        }
        String start = parts[0];
        String end = parts[1];

        int startRow = Integer.parseInt(start.replaceAll("[^0-9]", "")) - 1;
        int endRow = Integer.parseInt(end.replaceAll("[^0-9]", "")) - 1;
        int startCol = getColumnIndex(start.replaceAll("[0-9]", ""));
        int endCol = getColumnIndex(end.replaceAll("[0-9]", ""));

        double maxVal = Double.MIN_VALUE;
        for (int row = startRow; row <= endRow; row++) {
            for (int col = startCol; col <= endCol; col++) {
                double cellValue = getCellValue(tableView, ColumnNameUtils.getColumnName(col) + (row + 1));
                if (cellValue > maxVal) {
                    maxVal = cellValue;
                }
            }
        }
        return maxVal;
    }

    /**
     * Concatenates the values of multiple cells.
     *
     * @param tableView The TableView containing the cells
     * @param cellRefs  The cell references to concatenate
     * @return The concatenated cell values
     */
    public static String concat(TableView<ObservableList<String>> tableView, String... cellRefs) {
        StringBuilder result = new StringBuilder();
        for (String cellRef : cellRefs) {
            result.append(getCellText(tableView, cellRef));
        }
        return result.toString();
    }

    /**
     * Concatenates the values of a range of cells.
     *
     * @param tableView The TableView containing the cells
     * @param range     The range of cells to concatenate (e.g., "A1:A10")
     * @return The concatenated cell values
     */
    public static String concatRange(TableView<ObservableList<String>> tableView, String range) {
        String[] parts = range.split(":");
        if (parts.length != 2) {
            return "";
        }
        String start = parts[0];
        String end = parts[1];

        int startRow = Integer.parseInt(start.replaceAll("[^0-9]", "")) - 1;
        int endRow = Integer.parseInt(end.replaceAll("[^0-9]", "")) - 1;
        int startCol = getColumnIndex(start.replaceAll("[0-9]", ""));
        int endCol = getColumnIndex(end.replaceAll("[0-9]", ""));

        StringBuilder result = new StringBuilder();
        for (int row = startRow; row <= endRow; row++) {
            for (int col = startCol; col <= endCol; col++) {
                result.append(getCellText(tableView, ColumnNameUtils.getColumnName(col) + (row + 1)));
            }
        }
        return result.toString();
    }

    /**
     * Evaluates an IF function.
     *
     * @param tableView   The TableView containing the cells
     * @param conditionRef The cell reference for the condition
     * @param trueRef     The cell reference for the true case
     * @param falseRef    The cell reference for the false case
     * @return The result of the IF function
     */
    public static String ifFunction(TableView<ObservableList<String>> tableView, String conditionRef, String trueRef, String falseRef) {
        double conditionValue;
        try {
            conditionValue = getCellValue(tableView, conditionRef);
        } catch (NumberFormatException e) {
            return "ERROR";
        }

        if (conditionValue != 0) {
            return getCellText(tableView, trueRef);
        } else {
            return getCellText(tableView, falseRef);
        }
    }

    /**
     * Evaluates a DEBUG function.
     *
     * @param tableView  The TableView containing the cells
     * @param expression The cell reference for the expression to debug
     * @return The value of the expression
     */
    public static String debugFunction(TableView<ObservableList<String>> tableView, String expression) {
        return getCellText(tableView, expression);
    }

    /**
     * Removes formatting markers from a cell value.
     *
     * @param text The cell value
     * @return The cell value with the markers removed
     */
    private static String stripMarkers(String text) {
        return text.replaceAll("\\*B\\*|\\*I\\*|\\*F\\d+\\*|\\*C\\w+\\*|\\*T\\w+\\*", "");
    }

    /**
     * Converts a column index to a column name.
     *
     * @param index The column index
     * @return The column name
     */
    public static String getColumnName(int index) {
        StringBuilder columnName = new StringBuilder();
        while (index >= 0) {
            columnName.insert(0, (char) ('A' + index % 26));
            index = index / 26 - 1;
        }
        return columnName.toString();
    }
}
