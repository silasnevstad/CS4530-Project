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
                System.out.print("newValue: " + newValue);


                int row = cellEditEvent.getTablePosition().getRow();
                int col = cellEditEvent.getTablePosition().getColumn();

                tableView.getItems().get(row).set(col, newValue);

                System.out.println(tableView.getItems().get(row));
                System.out.println(tableView.getItems().get(row).get(col));



                if (newValue.startsWith("=")) {
                    System.out.println("= detected");


                    FormulaParser parser = new FormulaParser(tableView);
                    String result = parser.evaluateFormula(newValue);
                    System.out.println(result);


                    tableView.getItems().get(row).set(col, result);
                }
            });
        }
    }
}
