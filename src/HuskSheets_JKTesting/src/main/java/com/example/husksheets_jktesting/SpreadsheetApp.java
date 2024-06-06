package com.example.husksheets_jktesting;

import javafx.application.Application;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * Main application class for the spreadsheet
 */
public class SpreadsheetApp extends Application {
    private Button colorButton; // Button for setting cell background color
    private Button textColorButton; // Button for setting cell text color
    private Set<CellSpan> mergedCells = new HashSet<>(); // Set of merged cell spans

    @Override
    public void start(Stage primaryStage) {
        TableView<ObservableList<String>> tableView = new TableView<>();
        tableView.getSelectionModel().setCellSelectionEnabled(true); // Enable cell selection
        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE); // Enable multiple selection

        // Set up columns and data
        setupTableColumns(tableView, 27); // Setting up 27 columns
        populateTableData(tableView);

        new SpreadsheetController(tableView);

        // Create toolbar with bold, italicize, merge buttons, font size field, and import button
        ToolBar toolBar = new ToolBar();
        Button boldButton = new Button("Bold");
        Button italicButton = new Button("Italicize");
        Button mergeButton = new Button("Merge"); // Merge button
        TextField fontSizeField = new TextField();
        fontSizeField.setPromptText("Font Size");
        Button importButton = new Button("Import CSV"); // Import button

        colorButton = new Button();
        colorButton.setStyle("-fx-background-color: black; -fx-min-width: 20px; -fx-min-height: 20px;");
        ContextMenu colorMenu = new ContextMenu();
        MenuItem redItem = new MenuItem("Red", createColorSquare(Color.RED));
        MenuItem blueItem = new MenuItem("Blue", createColorSquare(Color.BLUE));
        MenuItem yellowItem = new MenuItem("Yellow", createColorSquare(Color.YELLOW));
        colorMenu.getItems().addAll(redItem, blueItem, yellowItem);

        colorButton.setOnAction(event -> colorMenu.show(colorButton, Side.BOTTOM, 0, 0));

        textColorButton = new Button();
        textColorButton.setStyle("-fx-background-color: black; -fx-min-width: 20px; -fx-min-height: 20px;");
        ContextMenu textColorMenu = new ContextMenu();
        MenuItem redTextItem = new MenuItem("Red", createColorSquare(Color.RED));
        MenuItem blueTextItem = new MenuItem("Blue", createColorSquare(Color.BLUE));
        MenuItem yellowTextItem = new MenuItem("Yellow", createColorSquare(Color.YELLOW));
        textColorMenu.getItems().addAll(redTextItem, blueTextItem, yellowTextItem);

        textColorButton.setOnAction(event -> textColorMenu.show(textColorButton, Side.BOTTOM, 0, 0));

        toolBar.getItems().addAll(boldButton, italicButton, mergeButton, fontSizeField, colorButton, textColorButton, importButton);

        // Handle bold button action
        boldButton.setOnAction(event -> toggleFormatting(tableView, "*B*"));

        // Handle italicize button action
        italicButton.setOnAction(event -> toggleFormatting(tableView, "*I*"));

        // Handle font size change
        fontSizeField.setOnAction(event -> {
            try {
                int fontSize = Integer.parseInt(fontSizeField.getText());
                applyFontSize(tableView, fontSize);
            } catch (NumberFormatException e) {
                System.out.println("Invalid font size: " + fontSizeField.getText());
            }
        });

        redItem.setOnAction(event -> applyCellColor(tableView, "red", colorButton));
        blueItem.setOnAction(event -> applyCellColor(tableView, "blue", colorButton));
        yellowItem.setOnAction(event -> applyCellColor(tableView, "yellow", colorButton));

        redTextItem.setOnAction(event -> applyTextColor(tableView, "red", textColorButton));
        blueTextItem.setOnAction(event -> applyTextColor(tableView, "blue", textColorButton));
        yellowTextItem.setOnAction(event -> applyTextColor(tableView, "yellow", textColorButton));

        // Handle merge button action
        mergeButton.setOnAction(event -> mergeSelectedCells(tableView));

        // Handle import button action
        importButton.setOnAction(event -> importCSV(tableView));

        VBox vbox = new VBox(toolBar, tableView);
        ScrollPane scrollPane = new ScrollPane(vbox); // Add scroll pane for horizontal scrolling
        scrollPane.setFitToWidth(true);

        Scene scene = new Scene(scrollPane, 800, 600);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Spreadsheet Application");
        primaryStage.show();
    }

    /**
     * Sets up columns for the TableView
     *
     * @param tableView   The TableView to set up columns for
     * @param columnCount The number of columns to set up
     */
    private void setupTableColumns(TableView<ObservableList<String>> tableView, int columnCount) {
        for (int i = 0; i < columnCount; i++) {
            TableColumn<ObservableList<String>, String> column = new TableColumn<>(ColumnNameUtils.getColumnName(i));
            final int colIndex = i;
            column.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().get(colIndex)));
            column.setCellFactory(new Callback<>() {
                @Override
                public TableCell<ObservableList<String>, String> call(TableColumn<ObservableList<String>, String> param) {
                    return new CustomTextFieldTableCell(colIndex, mergedCells);
                }
            });
            column.setOnEditCommit(event -> {
                int rowIndex = event.getTablePosition().getRow();
                int columnIndex = event.getTablePosition().getColumn();
                ObservableList<String> row = tableView.getItems().get(rowIndex);
                String newValue = event.getNewValue();
                row.set(columnIndex, newValue);
            });
            tableView.getColumns().add(column);
        }
    }

    /**
     * Populates TableView with initial data
     *
     * @param tableView The TableView to populate
     */
    private void populateTableData(TableView<ObservableList<String>> tableView) {
        for (int i = 0; i < 10; i++) {
            ObservableList<String> row = FXCollections.observableArrayList();
            for (int j = 0; j < 27; j++) { // Populate 27 columns with numeric values
                row.add(String.valueOf((i + 1) * (j + 1)));
            }
            tableView.getItems().add(row);
        }
    }

    /**
     * Creates a color square for color menu items
     *
     * @param color The color for the square
     * @return The HBox representing the color square
     */
    private HBox createColorSquare(Color color) {
        HBox box = new HBox();
        box.setStyle("-fx-background-color: " + toRgbString(color) + "; -fx-min-width: 15px; -fx-min-height: 15px;");
        return box;
    }

    /**
     * Converts a Color to its RGB string representation
     *
     * @param color The color to convert
     * @return The RGB string representation of the color
     */
    private String toRgbString(Color color) {
        return String.format("rgb(%d,%d,%d)", (int) (color.getRed() * 255), (int) (color.getGreen() * 255), (int) (color.getBlue() * 255));
    }

    /**
     * Applies cell background color to selected cells
     *
     * @param tableView   The TableView containing the cells
     * @param color       The color to apply
     * @param colorButton The button used to select the color
     */
    private void applyCellColor(TableView<ObservableList<String>> tableView, String color, Button colorButton) {
        applyFormattingToSelectedCells(tableView, cellValue -> applyMarker(cellValue, "C" + color));
        colorButton.setStyle("-fx-background-color: " + color + "; -fx-min-width: 20px; -fx-min-height: 20px;");
    }

    /**
     * Applies text color to selected cells
     *
     * @param tableView      The TableView containing the cells
     * @param color          The color to apply
     * @param textColorButton The button used to select the color
     */
    private void applyTextColor(TableView<ObservableList<String>> tableView, String color, Button textColorButton) {
        applyFormattingToSelectedCells(tableView, cellValue -> applyMarker(cellValue, "T" + color));
        textColorButton.setStyle("-fx-background-color: " + color + "; -fx-min-width: 20px; -fx-min-height: 20px;");
    }

    /**
     * Applies font size to selected cells
     *
     * @param tableView The TableView containing the cells
     * @param fontSize  The font size to apply
     */
    private void applyFontSize(TableView<ObservableList<String>> tableView, int fontSize) {
        applyFormattingToSelectedCells(tableView, cellValue -> applyMarker(cellValue, "F" + fontSize));
    }

    /**
     * Applies formatting to selected cells
     *
     * @param tableView The TableView containing the cells
     * @param formatter The formatter to apply to each cell
     */
    private void applyFormattingToSelectedCells(TableView<ObservableList<String>> tableView, CellFormatter formatter) {
        for (TablePosition pos : tableView.getSelectionModel().getSelectedCells()) {
            int rowIndex = pos.getRow();
            int colIndex = pos.getColumn();

            if (rowIndex >= 0 && colIndex >= 0) {
                ObservableList<String> row = tableView.getItems().get(rowIndex);
                String cellValue = row.get(colIndex);
                row.set(colIndex, formatter.format(cellValue));
            }
        }
        tableView.refresh(); // Refresh the table to apply the changes
    }

    /**
     * Adds a marker to a cell value for formatting
     *
     * @param text   The original cell value
     * @param marker The marker to add
     * @return The cell value with the marker added
     */
    private String applyMarker(String text, String marker) {
        text = removeMarker(text, marker.substring(0, 1));
        return "*" + marker + "*" + text;
    }

    /**
     * Removes a marker from a cell value
     *
     * @param text       The original cell value
     * @param markerType The marker type to remove
     * @return The cell value with the marker removed
     */
    private String removeMarker(String text, String markerType) {
        return text.replaceAll("\\*" + markerType + "\\w+\\*", "");
    }

    /**
     * Merges selected cells
     *
     * @param tableView The TableView containing the cells
     */
    private void mergeSelectedCells(TableView<ObservableList<String>> tableView) {
        ObservableList<TablePosition> selectedCells = tableView.getSelectionModel().getSelectedCells();
        if (selectedCells.isEmpty()) {
            return;
        }

        int startRow = selectedCells.get(0).getRow();
        int startCol = selectedCells.get(0).getColumn();
        int endRow = startRow;
        int endCol = startCol;

        for (TablePosition pos : selectedCells) {
            int rowIndex = pos.getRow();
            int colIndex = pos.getColumn();
            if (rowIndex > endRow) endRow = rowIndex;
            if (colIndex > endCol) endCol = colIndex;
        }

        mergedCells.add(new CellSpan(startRow, startCol, endRow, endCol));

        StringBuilder mergedContent = new StringBuilder();
        for (TablePosition pos : selectedCells) {
            int rowIndex = pos.getRow();
            int colIndex = pos.getColumn();
            ObservableList<String> row = tableView.getItems().get(rowIndex);
            String cellValue = row.get(colIndex);
            if (!cellValue.isEmpty()) {
                if (mergedContent.length() > 0) {
                    mergedContent.append(" ");
                }
                mergedContent.append(cellValue);
            }
        }

        ObservableList<String> startRowList = tableView.getItems().get(startRow);
        startRowList.set(startCol, mergedContent.toString());
        tableView.refresh();
    }

    /**
     * Imports a CSV file into the TableView
     *
     * @param tableView The TableView to import the CSV into
     */
    private void importCSV(TableView<ObservableList<String>> tableView) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            CSVImporter.importCSV(selectedFile, tableView);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Custom TableCell to handle bold, italic, font size, background color, text color formatting, and cell spanning
     */
    static class CustomTextFieldTableCell extends TextFieldTableCell<ObservableList<String>, String> {
        private final int columnIndex; // The column index of the cell
        private final Set<CellSpan> mergedCells; // Set of merged cell spans
        private boolean isBold; // Whether the cell is bold
        private boolean isItalic; // Whether the cell is italic
        private int fontSize = -1; // The font size of the cell
        private String bgColor = ""; // The background color of the cell
        private String textColor = ""; // The text color of the cell

        public CustomTextFieldTableCell(int columnIndex, Set<CellSpan> mergedCells) {
            super(new StringConverter<>() {
                @Override
                public String toString(String object) {
                    if (object != null) {
                        return object.replaceAll("\\*B\\*|\\*I\\*|\\*F\\d+\\*|\\*C\\w+\\*|\\*T\\w+\\*", "");
                    }
                    return object;
                }

                @Override
                public String fromString(String string) {
                    return string;
                }
            });
            this.columnIndex = columnIndex;
            this.mergedCells = mergedCells;
        }

        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (item != null && !empty) {
                isBold = item.contains("*B*");
                isItalic = item.contains("*I*");
                fontSize = item.matches(".*\\*F\\d+\\*.*") ? Integer.parseInt(item.replaceFirst(".*\\*F(\\d+)\\*.*", "$1")) : -1;
                bgColor = item.matches(".*\\*C\\w+\\*.*") ? item.replaceFirst(".*\\*C(\\w+)\\*.*", "$1") : "";
                textColor = item.matches(".*\\*T\\w+\\*.*") ? item.replaceFirst(".*\\*T(\\w+)\\*.*", "$1") : "";

                String style = "";
                if (isBold) {
                    style += "-fx-font-weight: bold;";
                }
                if (isItalic) {
                    style += "-fx-font-style: italic; -fx-font-family: 'Verdana';";
                }
                if (fontSize > 0) {
                    style += "-fx-font-size: " + fontSize + "px;";
                }
                if (!bgColor.isEmpty()) {
                    style += "-fx-background-color: " + bgColor + ";";
                }
                if (!textColor.isEmpty()) {
                    style += "-fx-text-fill: " + textColor + ";";
                }
                setStyle(style);
                setText(item.replaceAll("\\*B\\*|\\*I\\*|\\*F\\d+\\*|\\*C\\w+\\*|\\*T\\w+\\*", "")); // Remove markers for display

                // Handle cell spanning
                boolean inSpan = false;
                for (CellSpan span : mergedCells) {
                    if (span.isWithinSpan(getIndex(), columnIndex)) {
                        inSpan = true;
                        if (span.isTopLeftCell(getIndex(), columnIndex)) {
                            setText(item.replaceAll("\\*B\\*|\\*I\\*|\\*F\\d+\\*|\\*C\\w+\\*|\\*T\\w+\\*", ""));
                            setVisible(true);
                        } else {
                            setText(null);
                            setVisible(false);
                        }
                        break;
                    }
                }
                if (!inSpan) {
                    setVisible(true);
                }
            } else {
                setStyle(""); // Reset style
                setText(null);
                setVisible(true);
            }
        }

        @Override
        public void startEdit() {
            super.startEdit();
            if (getItem() != null) {
                setText(stripMarkers(getItem())); // Remove markers for editing
            }
        }

        @Override
        public void commitEdit(String newValue) {
            String originalValue = getItem();
            newValue = applyMarkers(newValue, originalValue);

            super.commitEdit(newValue);
            setItem(newValue);
        }

        /**
         * Applies markers for formatting to a cell value
         *
         * @param newValue      The new cell value
         * @param originalValue The original cell value
         * @return The cell value with the markers applied
         */
        private String applyMarkers(String newValue, String originalValue) {
            if (originalValue.contains("*B*") && !newValue.contains("*B*")) {
                newValue = "*B*" + newValue;
            }
            if (originalValue.contains("*I*") && !newValue.contains("*I*")) {
                newValue = "*I*" + newValue;
            }
            if (originalValue.matches(".*\\*F\\d+\\*.*") && !newValue.matches(".*\\*F\\d+\\*.*")) {
                newValue = originalValue.replaceFirst(".*\\*F(\\d+)\\*.*", "*F$1*") + newValue;
            }
            if (originalValue.matches(".*\\*C\\w+\\*.*") && !newValue.matches(".*\\*C\\w+\\*.*")) {
                newValue = originalValue.replaceFirst(".*\\*C(\\w+)\\*.*", "*C$1*") + newValue;
            }
            if (originalValue.matches(".*\\*T\\w+\\*.*") && !newValue.matches(".*\\*T\\w+\\*.*")) {
                newValue = originalValue.replaceFirst(".*\\*T(\\w+)\\*.*", "*T$1*") + newValue;
            }
            return newValue;
        }

        /**
         * Removes markers for formatting from a cell value
         *
         * @param text The cell value
         * @return The cell value with the markers removed
         */
        private String stripMarkers(String text) {
            return text.replaceAll("\\*B\\*|\\*I\\*|\\*F\\d+\\*|\\*C\\w+\\*|\\*T\\w+\\*", "");
        }
    }

    /**
     * Toggles formatting for bold or italic
     *
     * @param tableView The TableView containing the cells
     * @param marker    The marker to toggle
     */
    private void toggleFormatting(TableView<ObservableList<String>> tableView, String marker) {
        applyFormattingToSelectedCells(tableView, cellValue -> {
            if (!cellValue.contains(marker)) {
                return marker + cellValue; // Add marker
            } else {
                return cellValue.replace(marker, ""); // Remove marker
            }
        });
    }
}

// Functional interface for cell formatting
@FunctionalInterface
interface CellFormatter {
    String format(String cellValue);
}
