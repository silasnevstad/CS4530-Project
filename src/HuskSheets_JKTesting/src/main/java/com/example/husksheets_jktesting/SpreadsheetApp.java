package com.example.husksheets_jktesting;
import javafx.application.Application;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SpreadsheetApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        TableView<ObservableList<String>> tableView = new TableView<>();

        // Set up columns and data
        setupTableColumns(tableView);
        populateTableData(tableView);

        new SpreadsheetController(tableView);

        Scene scene = new Scene(new VBox(tableView), 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Spreadsheet Application");
        primaryStage.show();
    }

    private void setupTableColumns(TableView<ObservableList<String>> tableView) {
        for (int i = 0; i < 5; i++) {
            TableColumn<ObservableList<String>, String> column = new TableColumn<>("Column " + (char) ('A' + i));
            final int colIndex = i;
            column.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().get(colIndex)));
            column.setCellFactory(TextFieldTableCell.forTableColumn());
            tableView.getColumns().add(column);
        }
    }

    private void populateTableData(TableView<ObservableList<String>> tableView) {
        for (int i = 0; i < 10; i++) {
            ObservableList<String> row = FXCollections.observableArrayList();
            for (int j = 0; j < 5; j++) {
                row.add("0");
            }
            tableView.getItems().add(row);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
