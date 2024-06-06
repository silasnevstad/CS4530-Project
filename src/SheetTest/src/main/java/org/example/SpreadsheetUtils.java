package org.example;

import javafx.collections.ObservableList;
import javafx.scene.control.TableView;

public class SpreadsheetUtils {
    public static double getCellValue(TableView<ObservableList<String>> tableView, String cellRef) {
        int row = Integer.parseInt(cellRef.substring(1)) - 1;
        int col = cellRef.charAt(0) - 'A';
        String cellValue = tableView.getItems().get(row).get(col);

        // Strip formatting markers
        String strippedValue = stripMarkers(cellValue);

        if (strippedValue.matches("-?\\d+(\\.\\d+)?")) {
            return Double.parseDouble(strippedValue);
        }
        return 0.0; // Return 0 if the cell value is not a number
    }

    public static double sum(TableView<ObservableList<String>> tableView, String... cellRefs) {
        double total = 0;
        for (String cellRef : cellRefs) {
            total += getCellValue(tableView, cellRef);
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

    private static String stripMarkers(String text) {
        return text.replaceAll("\\*B\\*|\\*I\\*|\\*F\\d+\\*|\\*C\\w+\\*|\\*T\\w+\\*", "");
    }
}
