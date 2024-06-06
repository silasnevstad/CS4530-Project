package com.example.husksheets_jktesting;

import javafx.collections.ObservableList;
import javafx.scene.control.TableView;

public class SpreadsheetUtils {
    public static double getCellValue(TableView<ObservableList<String>> tableView, String cellRef) {
        int row = Integer.parseInt(cellRef.substring(1)) - 1;
        int col = getColumnIndex(cellRef.charAt(0));
        if (cellRef.length() > 2) {
            col = getColumnIndex(cellRef.charAt(0)) * 26 + getColumnIndex(cellRef.charAt(1));
        }
        String cellValue = tableView.getItems().get(row).get(col);

        // Strip formatting markers
        String strippedValue = stripMarkers(cellValue);

        if (strippedValue.matches("-?\\d+(\\.\\d+)?")) {
            return Double.parseDouble(strippedValue);
        }
        return 0.0; // Return 0 if the cell value is not a number
    }

    public static String getCellText(TableView<ObservableList<String>> tableView, String cellRef) {
        int row = Integer.parseInt(cellRef.substring(1)) - 1;
        int col = getColumnIndex(cellRef.charAt(0));
        if (cellRef.length() > 2) {
            col = getColumnIndex(cellRef.charAt(0)) * 26 + getColumnIndex(cellRef.charAt(1));
        }
        String cellValue = tableView.getItems().get(row).get(col);

        // Strip formatting markers
        return stripMarkers(cellValue);
    }

    public static double sum(TableView<ObservableList<String>> tableView, String... cellRefs) {
        double total = 0;
        for (String cellRef : cellRefs) {
            total += getCellValue(tableView, cellRef);
        }
        return total;
    }

    public static double sumRange(TableView<ObservableList<String>> tableView, String range) {
        String[] parts = range.split(":");
        if (parts.length != 2) {
            return 0;
        }
        String start = parts[0];
        String end = parts[1];

        int startRow = Integer.parseInt(start.substring(1)) - 1;
        int endRow = Integer.parseInt(end.substring(1)) - 1;
        int startCol = getColumnIndex(start.charAt(0));
        int endCol = getColumnIndex(end.charAt(0));

        if (start.length() > 2) {
            startCol = getColumnIndex(start.charAt(0)) * 26 + getColumnIndex(start.charAt(1));
        }
        if (end.length() > 2) {
            endCol = getColumnIndex(end.charAt(0)) * 26 + getColumnIndex(end.charAt(1));
        }

        double total = 0;
        for (int row = startRow; row <= endRow; row++) {
            for (int col = startCol; col <= endCol; col++) {
                total += getCellValue(tableView, ColumnNameUtils.getColumnName(col) + (row + 1));
            }
        }
        return total;
    }

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

    public static String concat(TableView<ObservableList<String>> tableView, String... cellRefs) {
        StringBuilder result = new StringBuilder();
        for (String cellRef : cellRefs) {
            result.append(getCellText(tableView, cellRef));
        }
        return result.toString();
    }

    public static String concatRange(TableView<ObservableList<String>> tableView, String range) {
        String[] parts = range.split(":");
        if (parts.length != 2) {
            return "";
        }
        String start = parts[0];
        String end = parts[1];

        int startRow = Integer.parseInt(start.substring(1)) - 1;
        int endRow = Integer.parseInt(end.substring(1)) - 1;
        int startCol = getColumnIndex(start.charAt(0));
        int endCol = getColumnIndex(end.charAt(0));

        if (start.length() > 2) {
            startCol = getColumnIndex(start.charAt(0)) * 26 + getColumnIndex(start.charAt(1));
        }
        if (end.length() > 2) {
            endCol = getColumnIndex(end.charAt(0)) * 26 + getColumnIndex(end.charAt(1));
        }

        StringBuilder result = new StringBuilder();
        for (int row = startRow; row <= endRow; row++) {
            for (int col = startCol; col <= endCol; col++) {
                result.append(getCellText(tableView, ColumnNameUtils.getColumnName(col) + (row + 1)));
            }
        }
        return result.toString();
    }

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

    public static String debugFunction(TableView<ObservableList<String>> tableView, String expression) {
        return getCellText(tableView, expression);
    }

    private static String stripMarkers(String text) {
        return text.replaceAll("\\*B\\*|\\*I\\*|\\*F\\d+\\*|\\*C\\w+\\*|\\*T\\w+\\*", "");
    }

    private static int getColumnIndex(char columnChar) {
        return columnChar - 'A';
    }

    public static String getColumnName(int index) {
        StringBuilder columnName = new StringBuilder();
        while (index >= 0) {
            columnName.insert(0, (char) ('A' + index % 26));
            index = index / 26 - 1;
        }
        return columnName.toString();
    }
}
