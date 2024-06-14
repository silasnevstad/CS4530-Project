package com.group12.husksheets.ui.utils;

//Owner:Jason King
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    public static void importCSV(File file, TableView<ObservableList<SimpleStringProperty>> tableView) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean isFirstLine = true;
            tableView.getColumns().clear();
            tableView.getItems().clear();

            while ((line = br.readLine()) != null) {
                String[] values = parseCSVLine(line);
                if (isFirstLine) {
                    // Create a dummy first column for row numbers
                    TableColumn<ObservableList<SimpleStringProperty>, String> rowNumCol = new TableColumn<>("");
                    rowNumCol.setCellFactory(colFactory -> new TableCell<>() {
                        @Override
                        protected void updateItem(String item, boolean empty) {
                            super.updateItem(item, empty);
                            if (!empty) {
                                setText(String.valueOf(getIndex() + 1));
                            } else {
                                setText(null);
                            }
                        }
                    });
                    tableView.getColumns().add(rowNumCol);

                    // Create data columns
                    for (int i = 0; i < values.length; i++) {
                        TableColumn<ObservableList<SimpleStringProperty>, String> column = new TableColumn<>(values[i]);
                        final int colIndex = i;
                        column.setCellValueFactory(data -> data.getValue().get(colIndex));
                        column.setCellFactory(TextFieldTableCell.forTableColumn());
                        tableView.getColumns().add(column);
                    }
                    isFirstLine = false;
                } else {
                    ObservableList<SimpleStringProperty> row = FXCollections.observableArrayList();
                    for (String value : values) {
                        row.add(new SimpleStringProperty(value));
                    }
                    tableView.getItems().add(row);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Parses a CSV line considering possible quoted values with commas.
     *
     * @param line The CSV line to parse
     * @return The parsed values as an array of strings
     */
    private static String[] parseCSVLine(String line) {
        if (line == null) {
            return new String[0];
        }

        // Handle quoted values with commas
        List<String> values = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder sb = new StringBuilder();

        for (char ch : line.toCharArray()) {
            if (ch == '"') {
                inQuotes = !inQuotes; // Toggle quotes
            } else if (ch == ',' && !inQuotes) {
                values.add(sb.toString().trim());
                sb.setLength(0); // Reset the StringBuilder
            } else {
                sb.append(ch);
            }
        }

        values.add(sb.toString().trim()); // Add the last value
        return values.toArray(new String[0]);
    }
}
