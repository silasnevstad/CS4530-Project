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
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class SpreadsheetApp extends Application {
    private Button colorButton;
    private Button textColorButton;

    @Override
    public void start(Stage primaryStage) {
        TableView<ObservableList<String>> tableView = new TableView<>();

        // Set up columns and data
        setupTableColumns(tableView);
        populateTableData(tableView);

        new SpreadsheetController(tableView);

        // Create toolbar with bold and italicize buttons and a text field
        ToolBar toolBar = new ToolBar();
        Button boldButton = new Button("Bold");
        Button italicButton = new Button("Italicize");
        TextField fontSizeField = new TextField();
        fontSizeField.setPromptText("Font Size");

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

        toolBar.getItems().addAll(boldButton, italicButton, fontSizeField, colorButton, textColorButton);

        // Handle bold button action
        boldButton.setOnAction(event -> toggleFormatting(tableView, "*B*"));

        // Handle italicize button action
        italicButton.setOnAction(event -> toggleFormatting(tableView, "*I*"));

        // Handle font size change
        fontSizeField.setOnAction(event -> {
            int rowIndex = tableView.getSelectionModel().getSelectedIndex();
            TablePosition pos = tableView.getFocusModel().getFocusedCell();
            int colIndex = pos.getColumn();

            if (rowIndex >= 0 && colIndex >= 0) {
                ObservableList<String> row = tableView.getItems().get(rowIndex);
                String cellValue = row.get(colIndex);
                try {
                    int fontSize = Integer.parseInt(fontSizeField.getText());
                    row.set(colIndex, applyMarker(cellValue, "F" + fontSize));
                    tableView.getColumns().get(colIndex).setVisible(false);
                    tableView.getColumns().get(colIndex).setVisible(true);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid font size: " + fontSizeField.getText());
                }
            }
        });

        redItem.setOnAction(event -> applyCellColor(tableView, "red", colorButton));
        blueItem.setOnAction(event -> applyCellColor(tableView, "blue", colorButton));
        yellowItem.setOnAction(event -> applyCellColor(tableView, "yellow", colorButton));

        redTextItem.setOnAction(event -> applyTextColor(tableView, "red", textColorButton));
        blueTextItem.setOnAction(event -> applyTextColor(tableView, "blue", textColorButton));
        yellowTextItem.setOnAction(event -> applyTextColor(tableView, "yellow", textColorButton));

        VBox vbox = new VBox(toolBar, tableView);
        Scene scene = new Scene(vbox, 800, 600);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Spreadsheet Application");
        primaryStage.show();
    }

    private void setupTableColumns(TableView<ObservableList<String>> tableView) {
        for (int i = 0; i < 5; i++) {
            TableColumn<ObservableList<String>, String> column = new TableColumn<>("Column " + (char) ('A' + i));
            final int colIndex = i;
            column.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().get(colIndex)));
            column.setCellFactory(new Callback<>() {
                @Override
                public TableCell<ObservableList<String>, String> call(TableColumn<ObservableList<String>, String> param) {
                    return new CustomTextFieldTableCell();
                }
            });
            column.setOnEditCommit(event -> {
                int rowIndex = event.getTablePosition().getRow();
                int colIndex = event.getTablePosition().getColumn();
                ObservableList<String> row = tableView.getItems().get(rowIndex);
                String newValue = event.getNewValue();
                row.set(colIndex, newValue);
            });
            tableView.getColumns().add(column);
        }
    }

    private void populateTableData(TableView<ObservableList<String>> tableView) {
        for (int i = 0; i < 10; i++) {
            ObservableList<String> row = FXCollections.observableArrayList();
            for (int j = 0; j < 5; j++) {
                row.add(String.valueOf((i + 1) * (j + 1))); // Populate with numeric values
            }
            tableView.getItems().add(row);
        }
    }

    private HBox createColorSquare(Color color) {
        HBox box = new HBox();
        box.setStyle("-fx-background-color: " + toRgbString(color) + "; -fx-min-width: 15px; -fx-min-height: 15px;");
        return box;
    }

    private String toRgbString(Color color) {
        return String.format("rgb(%d,%d,%d)", (int)(color.getRed()*255), (int)(color.getGreen()*255), (int)(color.getBlue()*255));
    }

    private void applyCellColor(TableView<ObservableList<String>> tableView, String color, Button colorButton) {
        int rowIndex = tableView.getSelectionModel().getSelectedIndex();
        TablePosition pos = tableView.getFocusModel().getFocusedCell();
        int colIndex = pos.getColumn();

        if (rowIndex >= 0 && colIndex >= 0) {
            ObservableList<String> row = tableView.getItems().get(rowIndex);
            String cellValue = row.get(colIndex);
            row.set(colIndex, applyMarker(cellValue, "C" + color));
            tableView.getColumns().get(colIndex).setVisible(false);
            tableView.getColumns().get(colIndex).setVisible(true);
            colorButton.setStyle("-fx-background-color: " + color + "; -fx-min-width: 20px; -fx-min-height: 20px;");
        }
    }

    private void applyTextColor(TableView<ObservableList<String>> tableView, String color, Button textColorButton) {
        int rowIndex = tableView.getSelectionModel().getSelectedIndex();
        TablePosition pos = tableView.getFocusModel().getFocusedCell();
        int colIndex = pos.getColumn();

        if (rowIndex >= 0 && colIndex >= 0) {
            ObservableList<String> row = tableView.getItems().get(rowIndex);
            String cellValue = row.get(colIndex);
            row.set(colIndex, applyMarker(cellValue, "T" + color));
            tableView.getColumns().get(colIndex).setVisible(false);
            tableView.getColumns().get(colIndex).setVisible(true);
            textColorButton.setStyle("-fx-background-color: " + color + "; -fx-min-width: 20px; -fx-min-height: 20px;");
        }
    }

    private String applyMarker(String text, String marker) {
        text = removeMarker(text, marker.substring(0, 1));
        return "*" + marker + "*" + text;
    }

    private String removeMarker(String text, String markerType) {
        return text.replaceAll("\\*" + markerType + "\\w+\\*", "");
    }

    public static void main(String[] args) {
        launch(args);
    }

    // Custom TableCell to handle bold, italic, font size, background color, and text color formatting
    static class CustomTextFieldTableCell extends TextFieldTableCell<ObservableList<String>, String> {
        private boolean isBold;
        private boolean isItalic;
        private int fontSize = -1;
        private String bgColor = "";
        private String textColor = "";

        public CustomTextFieldTableCell() {
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
            } else {
                setStyle(""); // Reset style
                setText(null);
            }
        }

        @Override
        public void startEdit() {
            super.startEdit();
            if (getItem() != null) {
                setText(getItem().replaceAll("\\*B\\*|\\*I\\*|\\*F\\d+\\*|\\*C\\w+\\*|\\*T\\w+\\*", "")); // Remove markers for editing
            }
        }

        @Override
        public void commitEdit(String newValue) {
            String originalValue = getItem();
            newValue = applyMarkers(newValue, originalValue);

            super.commitEdit(newValue);
            setItem(newValue);
        }

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
    }

    // Toggle formatting for bold or italic
    private void toggleFormatting(TableView<ObservableList<String>> tableView, String marker) {
        int rowIndex = tableView.getSelectionModel().getSelectedIndex();
        TablePosition pos = tableView.getFocusModel().getFocusedCell();
        int colIndex = pos.getColumn();

        if (rowIndex >= 0 && colIndex >= 0) {
            ObservableList<String> row = tableView.getItems().get(rowIndex);
            String cellValue = row.get(colIndex);
            if (!cellValue.contains(marker)) {
                row.set(colIndex, marker + cellValue); // Add marker
            } else {
                row.set(colIndex, cellValue.replace(marker, "")); // Remove marker
            }
            tableView.getColumns().get(colIndex).setVisible(false);
            tableView.getColumns().get(colIndex).setVisible(true);
        }
    }
}

