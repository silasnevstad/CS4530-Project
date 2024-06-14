// Owner: Zach Pulichino and Silas Nevstad
package com.group12.husksheets.ui.controllers;

import com.group12.husksheets.models.Argument;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import java.io.IOException;
import com.group12.husksheets.models.Result;
import com.group12.husksheets.ui.controllers.SheetSelectPageController;
import com.group12.husksheets.ui.services.BackendService;
import com.group12.husksheets.ui.utils.ArithmeticParser;
import com.group12.husksheets.ui.utils.CSVImporter;
import com.group12.husksheets.ui.utils.FormulaParser;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.converter.DefaultStringConverter;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.group12.husksheets.ui.utils.ColumnNameUtils.getColumnIndex;

public class SheetView {

    // Constants for initial number of rows and columns
    public static final int NUM_ROWS = 100;
    public static final int NUM_COLUMNS = 26;

    // TableView to hold the spreadsheet data
    public TableView<ObservableList<SimpleStringProperty>> tableView;

    // TextField to display and edit cell formulas
    public TextField formulaField;

    // TextField for the title of the spreadsheet
    public TextField titleField;

    // Stacks to manage undo and redo operations
    public Stack<EditAction> undoStack = new Stack<>();
    public Stack<EditAction> redoStack = new Stack<>();

    // Clipboard content for cut/copy/paste operations
    public Clipboard clipboard = Clipboard.getSystemClipboard();
    public ClipboardContent clipboardContent = new ClipboardContent();

    // Maps to store various attributes of the cells
    public Map<String, String> formulas = new HashMap<>();
    public Map<String, Pos> alignments = new HashMap<>();
    public Map<String, String> fontSizes = new HashMap<>();
    public Map<String, String> textColors = new HashMap<>();
    public Map<String, String> backgroundColors = new HashMap<>();
    public Map<String, String> fonts = new HashMap<>();
    public Map<String, Boolean> boldStyles = new HashMap<>();
    public Map<String, Boolean> italicStyles = new HashMap<>();
    public Map<String, String> styles = new HashMap<>();

    // Change tracker to keep track of changes
    private Map<String, String> changeTracker = new HashMap<>();

    // Instance of FormulaParser
    public FormulaParser formulaParser;

    private BackendService backendService;
    private String lastUpdateId = "0";
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public void showSpreadsheetView(Stage primaryStage, String publisherName, String sheetName, boolean isOwned) {
        BorderPane root = new BorderPane();

        // Initialize the TableView
        tableView = new TableView<>();
        tableView.setEditable(true);

        // Initialize the FormulaParser
        formulaParser = new FormulaParser(tableView);

        openBlankSheet();

        formulaField = new TextField();
        formulaField.setEditable(true);
        formulaField.setOnAction(e -> {
            if (!tableView.getSelectionModel().getSelectedCells().isEmpty()) {
                TablePosition selectedCell = tableView.getSelectionModel().getSelectedCells().get(0);
                ObservableList<SimpleStringProperty> row = tableView.getItems().get(selectedCell.getRow());
                String oldValue = row.get(selectedCell.getColumn() - 1).get();
                String newValue = formulaField.getText();
                undoStack.push(new EditAction(row, selectedCell.getColumn() - 1, oldValue, newValue, EditAction.ActionType.VALUE_CHANGE));
                row.get(selectedCell.getColumn() - 1).set(newValue);
                redoStack.clear();
                String cellKey = getCellKey(row, selectedCell.getColumn() - 1);
                formulas.put(cellKey, newValue);
                changeTracker.put(cellKey, newValue);
                evaluateCell(row, selectedCell.getColumn() - 1);
            }
        });

        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null && !tableView.getSelectionModel().getSelectedCells().isEmpty()) {
                TablePosition selectedCell = tableView.getSelectionModel().getSelectedCells().get(0);
                if (selectedCell.getColumn() > 0) {
                    String cellKey = getCellKey(newSelection, selectedCell.getColumn() - 1);
                    formulaField.setText(formulas.getOrDefault(cellKey, newSelection.get(selectedCell.getColumn() - 1).get()));
                }
            }
        });

        VBox topContainer = new VBox();

        titleField = new TextField("Untitled Spreadsheet");
        titleField.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        titleField.setEditable(true);
        titleField.setPrefWidth(400);
        HBox titleBox = new HBox(titleField);
        titleBox.setStyle("-fx-padding: 10px;");

        // Initialize the toolbar with various controls
        ToolBar toolBar = new ToolBar();
        Button undoButton = new Button("Undo");
        Button redoButton = new Button("Redo");
        Button importCsvButton = new Button("Import CSV");
        Button boldButton = new Button("Bold");
        Button italicButton = new Button("Italic");

        // Add "Back to Home" button
        Button backToHomeButton = new Button("Back to Home");

        // Add action to import CSV button
        importCsvButton.setOnAction(e -> importCSV());

        ComboBox<String> fontComboBox = new ComboBox<>();
        fontComboBox.setItems(FXCollections.observableArrayList(Font.getFontNames()));
        fontComboBox.setOnAction(e -> changeFont(fontComboBox.getValue()));
        TextField fontSizeField = new TextField();
        fontSizeField.setPrefWidth(50);
        fontSizeField.setOnAction(e -> changeFontSize(fontSizeField.getText()));
        ColorPicker textColorPicker = new ColorPicker();
        textColorPicker.setOnAction(e -> changeTextColor(textColorPicker.getValue()));
        ColorPicker backgroundColorPicker = new ColorPicker();
        backgroundColorPicker.setOnAction(e -> changeBackgroundColor(backgroundColorPicker.getValue()));

        // Set actions for the toolbar buttons
        undoButton.setOnAction(e -> undo());
        redoButton.setOnAction(e -> redo());
        boldButton.setOnAction(e -> toggleBold());
        italicButton.setOnAction(e -> toggleItalic());

        // Set action for "Back to Home" button
        backToHomeButton.setOnAction(e -> {
            checkForUpdatesAndSendChanges(publisherName, sheetName, isOwned);
            navigateToSheetSelect(primaryStage, publisherName);
        });

        // Add controls to the toolbar
        toolBar.getItems().addAll(undoButton, redoButton, importCsvButton, boldButton, italicButton,
            new Label("Font:"), fontComboBox, new Label("Size:"), fontSizeField,
            new Label("Text Color:"), textColorPicker, new Label("Background Color:"), backgroundColorPicker, backToHomeButton);

        topContainer.getChildren().addAll(titleBox, toolBar, formulaField);

        root.setTop(topContainer);
        root.setCenter(tableView);

        // Create context menu for right-click operations
        ContextMenu contextMenu = new ContextMenu();
        MenuItem cutItem = new MenuItem("Cut");
        MenuItem copyItem = new MenuItem("Copy");
        MenuItem pasteItem = new MenuItem("Paste");

        cutItem.setOnAction(e -> cut());
        copyItem.setOnAction(e -> copy());
        pasteItem.setOnAction(e -> paste());

        contextMenu.getItems().addAll(cutItem, copyItem, pasteItem);

        // Show context menu on right-click
        tableView.setOnContextMenuRequested((ContextMenuEvent event) -> {
            contextMenu.show(tableView, event.getScreenX(), event.getScreenY());
        });

        loadSheet(publisherName, sheetName);
        scheduler.scheduleAtFixedRate(() -> checkForUpdatesAndSendChanges(publisherName, sheetName, isOwned), 5, 5, TimeUnit.SECONDS);

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("HuskySheets");
        primaryStage.setMaximized(true);
        primaryStage.show();

        primaryStage.setUserData(this);
    }

    public void openBlankSheet() {
        tableView.getColumns().clear();
        for (int col = 0; col <= NUM_COLUMNS; col++) {
            TableColumn<ObservableList<SimpleStringProperty>, String> column;
            if (col == 0) {
                column = new TableColumn<>("");
                column.setCellFactory(colFactory -> new TableCell<>() {
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
            } else {
                final int colIndex = col - 1;
                column = new TableColumn<>(getColumnHeader(colIndex));
                column.setCellValueFactory(cellData -> cellData.getValue().get(colIndex));
                column.setCellFactory(colFactory -> new AlignedTextFieldTableCell());
                column.setOnEditCommit(event -> {
                    String oldValue = event.getOldValue();
                    String newValue = event.getNewValue();
                    undoStack.push(new EditAction(event.getRowValue(), colIndex, oldValue, newValue, EditAction.ActionType.VALUE_CHANGE));
                    event.getRowValue().get(colIndex).set(newValue);
                    redoStack.clear();
                    String cellKey = getCellKey(event.getRowValue(), colIndex);
                    formulas.put(cellKey, newValue);
                    changeTracker.put(cellKey, newValue);
                    evaluateCell(event.getRowValue(), colIndex);
                });
                column.setPrefWidth(100);
            }
            tableView.getColumns().add(column);
        }

        ObservableList<ObservableList<SimpleStringProperty>> data = FXCollections.observableArrayList();
        for (int row = 0; row < NUM_ROWS; row++) {
            ObservableList<SimpleStringProperty> rowData = FXCollections.observableArrayList();
            for (int col = 0; col < NUM_COLUMNS; col++) {
                rowData.add(new SimpleStringProperty(""));
            }
            data.add(rowData);
        }
        tableView.setItems(data);
    }

    private void saveSheetData() {
        StringBuilder data = new StringBuilder();
        for (int row = 0; row < tableView.getItems().size(); row++) {
            for (int col = 0; col < tableView.getColumns().size(); col++) {
                if (col == 0) continue; // Skip the row number column
                ObservableList<SimpleStringProperty> rowData = tableView.getItems().get(row);
                data.append(rowData.get(col).get()).append(",");
            }
            data.deleteCharAt(data.length() - 1); // Remove trailing comma
            data.append("\n");
        }
        // Save `data.toString()` to a file or send to backend
    }

    /**
     * Loads the entire sheet data from the server.
     *
     * @param publisher the publisher of the sheet
     * @param sheet the name of the sheet
     */
    private void loadSheet(String publisher, String sheet) {
        try {
            Result result = backendService.getAllUpdates(publisher, sheet, "0");
            if (result.success && result.value != null && !result.value.isEmpty()) {
                Argument arg = result.value.get(0);
                applyUpdates(arg.payload);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Fetch updates from the server and apply them to the sheet.
     *
     * @param publisher the publisher of the sheet
     * @param sheet the name of the sheet
     * @param id the last update ID
     * @param isOwned whether the sheet is owned by the user
     */
    private void fetchAndApplyUpdates(String publisher, String sheet, String id, boolean isOwned) {
        try {
            Result result = backendService.getUpdates(publisher, sheet, id, isOwned);
            if (result.success && result.value != null && !result.value.isEmpty()) {
                Argument arg = result.value.get(0);
                String payload =arg.payload;
                lastUpdateId = arg.id;
                applyUpdates(payload);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Apply updates to the sheet based on the payload received.
     * @param payload The payload containing the updates, separated by newlines
     */
    private void applyUpdates(String payload) {
        String[] updates = payload.split("\\$");

        for (int i = 1; i < updates.length; i++) {
            String update = updates[i];
            String[] parts = update.split(" ", 2);

            if (parts.length == 2) {
                String cell = parts[0];
                String value = parts[1];

                String columnPart = cell.replaceAll("\\d", "");
                String rowPart = cell.replaceAll("\\D", "");

                int col = getColumnIndex(columnPart);
                int row = Integer.parseInt(rowPart) - 1;

                if (row < NUM_ROWS && col < NUM_COLUMNS) {
                    ObservableList<SimpleStringProperty> rowData = tableView.getItems().get(row);
                    rowData.get(col).set(value);
                }
            }
        }
    }

    /**
     * Check for updates from the server and send local changes if any.
     *
     * @param publisherName the publisher of the sheet
     * @param sheetName the name of the sheet
     * @param isOwned whether the sheet is owned by the user
     */
    private void checkForUpdatesAndSendChanges(String publisherName, String sheetName, boolean isOwned) {
        try {
            // Fetch updates from the server
            fetchAndApplyUpdates(publisherName, sheetName, lastUpdateId, isOwned);

            // Collect local changes and send to the server
            String payload = collectLocalChanges();
            if (!payload.isEmpty()) {
                backendService.updateSheet(publisherName, sheetName, payload, isOwned);
                changeTracker.clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Collect local changes made to the sheet.
     *
     * @return the payload containing the changes
     */
    private String collectLocalChanges() {
        StringBuilder changes = new StringBuilder();

        for (Map.Entry<String, String> entry : changeTracker.entrySet()) {
            String cell = getColumnHeader(Integer.parseInt(entry.getKey().split(",")[1])) + (Integer.parseInt(entry.getKey().split(",")[0]) + 1);
            changes.append("$").append(cell).append(" ").append(entry.getValue());
        }
        return changes.toString().trim();
    }

    /**
     * Generate a column header label based on the column index.
     *
     * @param index the column index
     * @return the column header label
     */
    public String getColumnHeader(int index) {
        StringBuilder sb = new StringBuilder();
        while (index >= 0) {
            sb.insert(0, (char) ('A' + (index % 26)));
            index = (index / 26) - 1;
        }
        return sb.toString();
    }

    /**
     * Undo the last action.
     */
    public void undo() {
        if (!undoStack.isEmpty()) {
            EditAction action = undoStack.pop();
            action.undo();
            redoStack.push(action);
        }
    }

    /**
     * Redo the last undone action.
     */
    public void redo() {
        if (!redoStack.isEmpty()) {
            EditAction action = redoStack.pop();
            action.redo();
            undoStack.push(action);
        }
    }

    /**
     * Cut the selected cell's content.
     */
    public void cut() {
        if (!tableView.getSelectionModel().getSelectedCells().isEmpty()) {
            TablePosition selectedCell = tableView.getSelectionModel().getSelectedCells().get(0);
            ObservableList<SimpleStringProperty> row = tableView.getItems().get(selectedCell.getRow());
            clipboardContent.putString(row.get(selectedCell.getColumn()).get());
            clipboard.setContent(clipboardContent);
            undoStack.push(new EditAction(row, selectedCell.getColumn(), clipboardContent.getString(), "", EditAction.ActionType.VALUE_CHANGE));
            row.get(selectedCell.getColumn()).set("");
            redoStack.clear();
        }
    }

    /**
     * Copy the selected cell's content.
     */
    public void copy() {
        if (!tableView.getSelectionModel().getSelectedCells().isEmpty()) {
            TablePosition selectedCell = tableView.getSelectionModel().getSelectedCells().get(0);
            ObservableList<SimpleStringProperty> row = tableView.getItems().get(selectedCell.getRow());
            clipboardContent.putString(row.get(selectedCell.getColumn()).get());
            clipboard.setContent(clipboardContent);
        }
    }

    /**
     * Paste the clipboard content into the selected cell.
     */
    public void paste() {
        if (!tableView.getSelectionModel().getSelectedCells().isEmpty()) {
            TablePosition selectedCell = tableView.getSelectionModel().getSelectedCells().get(0);
            ObservableList<SimpleStringProperty> row = tableView.getItems().get(selectedCell.getRow());
            String oldValue = row.get(selectedCell.getColumn()).get();
            String newValue = clipboard.getString();
            undoStack.push(new EditAction(row, selectedCell.getColumn(), oldValue, newValue, EditAction.ActionType.VALUE_CHANGE));
            row.get(selectedCell.getColumn()).set(newValue);
            redoStack.clear();
            String cellKey = getCellKey(row, selectedCell.getColumn());
            formulas.put(cellKey, newValue);
            evaluateCell(row, selectedCell.getColumn());
        }
    }

    /**
     * Evaluate the formula in a cell and update its value.
     *
     * @param row the row containing the cell
     * @param column the column index of the cell
     */
    public void evaluateCell(ObservableList<SimpleStringProperty> row, int column) {
        String cellKey = getCellKey(row, column);
        String formula = formulas.getOrDefault(cellKey, "");
        if (formula.isEmpty()) {
            row.get(column).set("");
            return;
        }
        try {
            String result;
            if (formula.startsWith("=")) {
                result = formulaParser.evaluateFormula(formula);
            } else {
                result = ArithmeticParser.isArithmeticExpression(formula) ?
                    String.valueOf(ArithmeticParser.evaluate(formula)) : formula;
            }
            row.get(column).set(result);
        } catch (Exception e) {
            row.get(column).set("#REF!");
        }
    }

    /**
     * Generate a unique key for a cell based on its row and column.
     *
     * @param row the row containing the cell
     * @param column the column index of the cell
     * @return the unique key for the cell
     */
    public String getCellKey(ObservableList<SimpleStringProperty> row, int column) {
        int rowIndex = tableView.getItems().indexOf(row);
        return rowIndex + "," + column;
    }

    /**
     * Change the font of the selected cell.
     *
     * @param font the new font to apply
     */
    public void changeFont(String font) {
        if (!tableView.getSelectionModel().getSelectedCells().isEmpty()) {
            TablePosition selectedCell = tableView.getSelectionModel().getSelectedCells().get(0);
            ObservableList<SimpleStringProperty> row = tableView.getItems().get(selectedCell.getRow());
            String cellKey = getCellKey(row, selectedCell.getColumn());
            undoStack.push(new EditAction(row, selectedCell.getColumn(), fonts.get(cellKey), font, EditAction.ActionType.FONT_CHANGE));
            fonts.put(cellKey, font);
            updateCellStyle(cellKey);
            redoStack.clear();
        }
    }

    /**
     * Change the font size of the selected cell.
     *
     * @param fontSize the new font size to apply
     */
    public void changeFontSize(String fontSize) {
        if (!tableView.getSelectionModel().getSelectedCells().isEmpty()) {
            TablePosition selectedCell = tableView.getSelectionModel().getSelectedCells().get(0);
            ObservableList<SimpleStringProperty> row = tableView.getItems().get(selectedCell.getRow());
            String cellKey = getCellKey(row, selectedCell.getColumn());
            undoStack.push(new EditAction(row, selectedCell.getColumn(), fontSizes.get(cellKey), fontSize, EditAction.ActionType.FONT_SIZE_CHANGE));
            fontSizes.put(cellKey, fontSize);
            updateCellStyle(cellKey);
            redoStack.clear();
        }
    }

    /**
     * Change the text color of the selected cell.
     *
     * @param color the new text color to apply
     */
    public void changeTextColor(Color color) {
        if (!tableView.getSelectionModel().getSelectedCells().isEmpty()) {
            TablePosition selectedCell = tableView.getSelectionModel().getSelectedCells().get(0);
            ObservableList<SimpleStringProperty> row = tableView.getItems().get(selectedCell.getRow());
            String cellKey = getCellKey(row, selectedCell.getColumn());
            String colorString = toRgbString(color);
            undoStack.push(new EditAction(row, selectedCell.getColumn(), textColors.get(cellKey), colorString, EditAction.ActionType.TEXT_COLOR_CHANGE));
            textColors.put(cellKey, colorString);
            updateCellStyle(cellKey);
            redoStack.clear();
        }
    }

    /**
     * Change the background color of the selected cell.
     *
     * @param color the new background color to apply
     */
    public void changeBackgroundColor(Color color) {
        if (!tableView.getSelectionModel().getSelectedCells().isEmpty()) {
            TablePosition selectedCell = tableView.getSelectionModel().getSelectedCells().get(0);
            ObservableList<SimpleStringProperty> row = tableView.getItems().get(selectedCell.getRow());
            String cellKey = getCellKey(row, selectedCell.getColumn());
            String colorString = toRgbString(color);
            undoStack.push(new EditAction(row, selectedCell.getColumn(), backgroundColors.get(cellKey), colorString, EditAction.ActionType.BACKGROUND_COLOR_CHANGE));
            backgroundColors.put(cellKey, colorString);
            updateCellStyle(cellKey);
            redoStack.clear();
        }
    }

    /**
     * Update the style of a cell based on its attributes.
     *
     * @param cellKey the unique key of the cell
     */
    public void updateCellStyle(String cellKey) {
        StringBuilder style = new StringBuilder();
        if (fontSizes.containsKey(cellKey)) {
            style.append("-fx-font-size: ").append(fontSizes.get(cellKey)).append(";");
        }
        if (fonts.containsKey(cellKey)) {
            style.append("-fx-font-family: '").append(fonts.get(cellKey)).append("';");
        }
        if (textColors.containsKey(cellKey)) {
            style.append("-fx-text-fill: ").append(textColors.get(cellKey)).append(";");
        }
        if (backgroundColors.containsKey(cellKey)) {
            style.append("-fx-background-color: ").append(backgroundColors.get(cellKey)).append(";");
        }
        if (boldStyles.getOrDefault(cellKey, false)) {
            style.append("-fx-font-weight: bold;");
        }
        if (italicStyles.getOrDefault(cellKey, false)) {
            style.append("-fx-font-style: italic;");
        }
        styles.put(cellKey, style.toString());
        tableView.refresh();
    }

    /**
     * Convert a Color object to an RGB string.
     *
     * @param color the Color object to convert
     * @return the RGB string representation of the color
     */
    public String toRgbString(Color color) {
        return "rgb(" + (int) (color.getRed() * 255) + "," + (int) (color.getGreen() * 255) + "," + (int) (color.getBlue() * 255) + ")";
    }

    /**
     * Open a file chooser to select and import a CSV file.
     */
    public void importCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showOpenDialog(tableView.getScene().getWindow());
        if (file != null) {
            CSVImporter.importCSV(file, tableView);
        }
    }

    /**
     * Toggle bold style for the selected cells.
     */
    public void toggleBold() {
        if (!tableView.getSelectionModel().getSelectedCells().isEmpty()) {
            for (TablePosition selectedCell : tableView.getSelectionModel().getSelectedCells()) {
                ObservableList<SimpleStringProperty> row = tableView.getItems().get(selectedCell.getRow());
                String cellKey = getCellKey(row, selectedCell.getColumn());
                boolean isBold = boldStyles.getOrDefault(cellKey, false);
                boldStyles.put(cellKey, !isBold);
                updateCellStyle(cellKey);
                String cellValue = row.get(selectedCell.getColumn()).get();
                row.get(selectedCell.getColumn()).set(cellValue); // Trigger the update
            }
        }
    }

    /**
     * Toggle italic style for the selected cells.
     */
    public void toggleItalic() {
        if (!tableView.getSelectionModel().getSelectedCells().isEmpty()) {
            for (TablePosition selectedCell : tableView.getSelectionModel().getSelectedCells()) {
                ObservableList<SimpleStringProperty> row = tableView.getItems().get(selectedCell.getRow());
                String cellKey = getCellKey(row, selectedCell.getColumn());
                boolean isItalic = italicStyles.getOrDefault(cellKey, false);
                italicStyles.put(cellKey, !isItalic);
                updateCellStyle(cellKey);
                String cellValue = row.get(selectedCell.getColumn()).get();
                row.get(selectedCell.getColumn()).set(cellValue); // Trigger the update
            }
        }
    }

    public void setBackendService(BackendService backendService) {
        this.backendService = backendService;
    }

    public void setTableView(TableView<ObservableList<SimpleStringProperty>> tableView) {
        this.tableView = tableView;
    }

    // Class representing an edit action for undo/redo functionality
    public class EditAction {
        // Enum to specify the type of action
        enum ActionType {
            VALUE_CHANGE, ALIGNMENT_CHANGE, FONT_SIZE_CHANGE, TEXT_COLOR_CHANGE, BACKGROUND_COLOR_CHANGE, FONT_CHANGE
        }

        private final ObservableList<SimpleStringProperty> row;
        private final int column;
        private final String oldValue;
        private final String newValue;
        private final ActionType actionType;

        public EditAction(ObservableList<SimpleStringProperty> row, int column, String oldValue, String newValue, ActionType actionType) {
            this.row = row;
            this.column = column;
            this.oldValue = oldValue;
            this.newValue = newValue;
            this.actionType = actionType;
        }

        // Method to undo the action
        public void undo() {
            apply(oldValue);
        }

        // Method to redo the action
        public void redo() {
            apply(newValue);
        }

        // Method to apply the action
        public void apply(String value) {
            String cellKey = getCellKey(row, column);
            switch (actionType) {
                case VALUE_CHANGE:
                    row.get(column).set(value);
                    break;
                case ALIGNMENT_CHANGE:
                    alignments.put(cellKey, Pos.valueOf(value));
                    tableView.refresh();
                    break;
                case FONT_SIZE_CHANGE:
                    fontSizes.put(cellKey, value);
                    updateCellStyle(cellKey);
                    tableView.refresh();
                    break;
                case TEXT_COLOR_CHANGE:
                    textColors.put(cellKey, value);
                    updateCellStyle(cellKey);
                    tableView.refresh();
                    break;
                case BACKGROUND_COLOR_CHANGE:
                    backgroundColors.put(cellKey, value);
                    updateCellStyle(cellKey);
                    tableView.refresh();
                    break;
                case FONT_CHANGE:
                    fonts.put(cellKey, value);
                    updateCellStyle(cellKey);
                    tableView.refresh();
                    break;
            }
        }
    }

    // Custom TableCell class to support cell alignment
    public class AlignedTextFieldTableCell extends TextFieldTableCell<ObservableList<SimpleStringProperty>, String> {
        private Pos cellAlignment = Pos.CENTER_LEFT;

        public AlignedTextFieldTableCell() {
            super(new DefaultStringConverter());
        }

        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);

            if (!empty) {
                String cellKey = getCellKey(getTableRow().getItem(), getTableView().getColumns().indexOf(getTableColumn()));
                setCellAlignment(alignments.getOrDefault(cellKey, Pos.CENTER_LEFT));
                setStyle(styles.getOrDefault(cellKey, ""));
                setText(item);
            } else {
                setText(null);
                setStyle("");
            }
        }

        public void setCellAlignment(Pos alignment) {
            this.cellAlignment = alignment;
            setStyle(getStyle() + "-fx-alignment: " + alignment.toString().replace("_", "-").toLowerCase() + ";");
        }
    }

    // Method to navigate back to the sheet select page
    private void navigateToSheetSelect(Stage stage, String publisherName) {
        SheetSelectPageController controller = new SheetSelectPageController();
        controller.setStage(stage);
        controller.setPublisherName(publisherName);
        controller.setBackendService(backendService);
        controller.run();
    }
}
