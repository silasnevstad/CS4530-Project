package com.example.husksheets_jktesting;

import javafx.collections.ObservableList;
import javafx.scene.control.TableView;

public class SpreadsheetUtils {
    // Method to get the value from a cell reference, assuming tableView is your TableView object
    public static double getCellValue(TableView<ObservableList<String>> tableView, String cellRef) {
        // Extract row and column from the cell reference (e.g., A1, B2, etc.)
        int row = Integer.parseInt(cellRef.substring(1)) - 1;
        System.out.println("Row is : "+ row);
        int col = cellRef.charAt(0) - 'A';
        System.out.println("Col is : "+ col);


        // Get the cell value
       // String cellValue = tableView.getItems().get(row).get(col);
        // Get the cell value
        ObservableList<String> rowData = tableView.getItems().get(row);
        for(String i : rowData){
            System.out.println(i);
        }

       // int column = tableView.getColumns().get(col);

        String cellValue = rowData.get(col);
        System.out.println("cellValue is : "+ cellValue);
        return Double.parseDouble(cellValue);
    }

    // SUM method that takes a variable number of cell references and returns their sum
    public static double sum(TableView<ObservableList<String>> tableView, String... cellRefs) {
        double total = 0;
        for (String cellRef : cellRefs) {
            System.out.println("Learn to Debug, Cellref is : " + cellRef );
            total += getCellValue(tableView, cellRef);
        }
        return total;
    }
}