package org.example;

package com.example.husksheets_jktesting;

import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class SpreadsheetController {
    private TableView<ObservableList<String>> tableView;

    public SpreadsheetController(TableView<ObservableList<String>> tableView) {
        this.tableView = tableView;
        initialize();
    }

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

    private String stripMarkers(String text) {
        return text.replaceAll("\\*B\\*|\\*I\\*|\\*F\\d+\\*|\\*C\\w+\\*|\\*T\\w+\\*", "");
    }
}
