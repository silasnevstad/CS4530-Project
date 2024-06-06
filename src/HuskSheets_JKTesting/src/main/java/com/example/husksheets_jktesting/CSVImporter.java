package com.example.husksheets_jktesting;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Utility class to import CSV data into a TableView
 */
public class CSVImporter {
    /**
     * Imports CSV data into a TableView
     *
     * @param file      The CSV file to import
     * @param tableView The TableView to populate
     */
    public static void importCSV(File file, TableView<ObservableList<String>> tableView) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean isFirstLine = true;
            tableView.getColumns().clear();
            tableView.getItems().clear();

            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (isFirstLine) {
                    for (int i = 0; i < values.length; i++) {
                        TableColumn<ObservableList<String>, String> column = new TableColumn<>(values[i]);
                        final int colIndex = i;
                        column.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().get(colIndex)));
                        column.setCellFactory(TextFieldTableCell.forTableColumn());
                        tableView.getColumns().add(column);
                    }
                    isFirstLine = false;
                } else {
                    ObservableList<String> row = FXCollections.observableArrayList(values);
                    tableView.getItems().add(row);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
