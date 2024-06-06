package com.example.husksheets_jktesting;

import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * Controller class for the spreadsheet
 */
public class SpreadsheetController {
    private TableView<ObservableList<String>> tableView; // The TableView containing the spreadsheet data

    /**
     * Constructs a SpreadsheetController object
     *
     * @param tableView The TableView to control
     */
    public SpreadsheetController(TableView<ObservableList<String>> tableView) {
        this.tableView = tableView;
        initialize();
    }

    /**
     * Initializes the controller
     */
    private void initialize() {
        tableView.setEditable(true);

        for (TableColumn<ObservableList<String>, ?> column : tableView.getColumns()) {
            column.setOnEditCommit(event -> {
                TableColumn.CellEditEvent<ObservableList<String>, String> cellEditEvent =
                        (TableColumn.CellEditEvent<ObservableList<String>, String>) event;
                String newValue = cellEditEvent.getNewValue();

                int row = cellEditEvent.getTablePosition().getRow();
                int col = cellEditEvent.getTablePosition().getColumn();

                // Strip formatting markers before processing
                String strippedValue = stripMarkers(newValue);

                if (strippedValue.startsWith("=")) {
                    FormulaParser parser = new FormulaParser(tableView);
                    String result = parser.evaluateFormula(strippedValue);
                    tableView.getItems().get(row).set(col, result);
                } else {
                    tableView.getItems().get(row).set(col, newValue);
                }
            });
        }
    }

    /**
     * Removes formatting markers from a cell value
     *
     * @param text The cell value
     * @return The cell value with the markers removed
     */
    private String stripMarkers(String text) {
        return text.replaceAll("\\*B\\*|\\*I\\*|\\*F\\d+\\*|\\*C\\w+\\*|\\*T\\w+\\*", "");
    }
}
