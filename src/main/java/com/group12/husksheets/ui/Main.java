package com.group12.husksheets.ui;

import com.group12.husksheets.models.Result;
import javafx.application.Application;
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

import static com.group12.husksheets.ui.ColumnNameUtils.getColumnIndex;

public class Main extends Application {

  // Constants for initial number of rows and columns
  private static final int NUM_ROWS = 100;
  private static final int NUM_COLUMNS = 26;

  // TableView to hold the spreadsheet data
  private TableView<ObservableList<SimpleStringProperty>> tableView;

  // TextField to display and edit cell formulas
  private TextField formulaField;

  // TextField for the title of the spreadsheet
  private TextField titleField;

  // Stacks to manage undo and redo operations
  private Stack<EditAction> undoStack = new Stack<>();
  private Stack<EditAction> redoStack = new Stack<>();

  // Clipboard content for cut/copy/paste operations
  private Clipboard clipboard = Clipboard.getSystemClipboard();
  private ClipboardContent clipboardContent = new ClipboardContent();

  // Maps to store various attributes of the cells
  private Map<String, String> formulas = new HashMap<>();
  private Map<String, Pos> alignments = new HashMap<>();
  private Map<String, String> fontSizes = new HashMap<>();
  private Map<String, String> textColors = new HashMap<>();
  private Map<String, String> backgroundColors = new HashMap<>();
  private Map<String, String> fonts = new HashMap<>();
  private Map<String, Boolean> boldStyles = new HashMap<>();
  private Map<String, Boolean> italicStyles = new HashMap<>();
  private Map<String, String> styles = new HashMap<>();

  // Change tracker to keep track of changes
  private Map<String, String> changeTracker = new HashMap<>();

  // Instance of FormulaParser
  private FormulaParser formulaParser;

  // Instance of BackendService
  private BackendService backendService;
  private String lastUpdateId = "0";
  private final String publisherName = "publisherName1";
  private final String sheetName = "sheetName1";
  private final boolean isOwned = true; // Does the client own the sheet?
  private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

  @Override
  public void start(Stage primaryStage) {
    BorderPane root = new BorderPane();

    // Initialize the BackendService
    backendService = new BackendService("user1", "password1");

    // Initialize the TableView
    tableView = new TableView<>();
    tableView.setEditable(true);

    // Initialize the FormulaParser
    formulaParser = new FormulaParser(tableView);

    // Create columns for the TableView
    for (int col = 0; col <= NUM_COLUMNS; col++) {
      TableColumn<ObservableList<SimpleStringProperty>, String> column;
      if (col == 0) {
        // Create the row number column
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
        // Create data columns
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

    // Create data for the TableView
    ObservableList<ObservableList<SimpleStringProperty>> data = FXCollections.observableArrayList();
    for (int row = 0; row < NUM_ROWS; row++) {
      ObservableList<SimpleStringProperty> rowData = FXCollections.observableArrayList();
      for (int col = 0; col < NUM_COLUMNS; col++) {
        rowData.add(new SimpleStringProperty(""));
      }
      data.add(rowData);
    }
    tableView.setItems(data);

    // Initialize the formula field
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

    // Update the formula field when a new cell is selected
    tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
      if (newSelection != null && !tableView.getSelectionModel().getSelectedCells().isEmpty()) {
        TablePosition selectedCell = tableView.getSelectionModel().getSelectedCells().get(0);
        if (selectedCell.getColumn() > 0) { // Ensure the column index is valid
          String cellKey = getCellKey(newSelection, selectedCell.getColumn() - 1);
          formulaField.setText(formulas.getOrDefault(cellKey, newSelection.get(selectedCell.getColumn() - 1).get()));
        }
      }
    });

    VBox topContainer = new VBox();

    // Initialize the title field
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

    // Add controls to the toolbar
    toolBar.getItems().addAll(undoButton, redoButton, importCsvButton, boldButton, italicButton,
        new Label("Font:"), fontComboBox, new Label("Size:"), fontSizeField,
        new Label("Text Color:"), textColorPicker, new Label("Background Color:"), backgroundColorPicker);

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

    // Call to fetch updates when a sheet is opened
    // Might be unnecessary since scheduler "becomes enabled first after the given initial delay"
//    fetchAndApplyUpdates(publisherName, sheetName, lastUpdateId, isOwned);

    // Schedule periodic tasks
    scheduler.scheduleAtFixedRate(this::checkForUpdatesAndSendChanges, 0, 10, TimeUnit.SECONDS);

    Scene scene = new Scene(root);
    primaryStage.setScene(scene);
    primaryStage.setTitle("HuskySheets");
    primaryStage.setMaximized(true);
    primaryStage.show();

    // Store reference to this instance for later access
    primaryStage.setUserData(this);
  }

  /**
   * Fetch updates from the backend service and apply them to the sheet.
   *
   * @param publisher The name of the publisher
   * @param sheet The name of the sheet
   * @param id The last id received ("0" for initial fetch)
   */
  private void fetchAndApplyUpdates(String publisher, String sheet, String id, boolean isOwned) {
    try {
      Result result = backendService.getUpdates(publisher, sheet, id, isOwned);
      if (result.success && result.value != null && !result.value.isEmpty()) {
        String payload = result.value.get(0).payload;
        lastUpdateId = result.value.get(0).id;
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
    String[] updates = payload.split("\n");
    for (String update : updates) {
      String[] parts = update.split(" ", 2);
      if (parts.length == 2) {
        String cell = parts[0].substring(1);  // Remove the dollar sign
        String value = parts[1];

        // Separate the column part and row part of the cell reference
        String columnPart = cell.replaceAll("\\d", "");
        String rowPart = cell.replaceAll("\\D", "");

        int col = getColumnIndex(columnPart);
        int row = Integer.parseInt(rowPart) - 1;  // Convert to 0-based index

        System.out.println("Applying update to cell " + cell + " with value " + value);

        if (row < NUM_ROWS && col < NUM_COLUMNS) {
          ObservableList<SimpleStringProperty> rowData = tableView.getItems().get(row);
          rowData.get(col).set(value);
        }
      }
    }
  }

  /**
   * Check for updates from the server and send local changes if any.
   */
  private void checkForUpdatesAndSendChanges() {
    try {
      // Fetch updates from the server
      fetchAndApplyUpdates(publisherName, sheetName, lastUpdateId, isOwned);

      // Collect local changes and send to the server
      String payload = collectLocalChanges();
      if (!payload.isEmpty()) {
        backendService.updateSheet(publisherName, sheetName, payload, isOwned);
        changeTracker.clear(); // Clear the change tracker after sending changes
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
      changes.append("$").append(cell).append(" ").append(entry.getValue()).append("\n");
    }

    return changes.toString().trim();
  }

  /**
   * Generate a column header label based on the column index.
   *
   * @param index the column index
   * @return the column header label
   */
  private String getColumnHeader(int index) {
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
  private void undo() {
    if (!undoStack.isEmpty()) {
      EditAction action = undoStack.pop();
      action.undo();
      redoStack.push(action);
    }
  }

  /**
   * Redo the last undone action.
   */
  private void redo() {
    if (!redoStack.isEmpty()) {
      EditAction action = redoStack.pop();
      action.redo();
      undoStack.push(action);
    }
  }

  /**
   * Cut the selected cell's content.
   */
  private void cut() {
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
  private void copy() {
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
  private void paste() {
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
  private void evaluateCell(ObservableList<SimpleStringProperty> row, int column) {
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
  private String getCellKey(ObservableList<SimpleStringProperty> row, int column) {
    int rowIndex = tableView.getItems().indexOf(row);
    return rowIndex + "," + column;
  }

  /**
   * Change the font of the selected cell.
   *
   * @param font the new font to apply
   */
  private void changeFont(String font) {
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
  private void changeFontSize(String fontSize) {
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
  private void changeTextColor(Color color) {
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
  private void changeBackgroundColor(Color color) {
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
  private void updateCellStyle(String cellKey) {
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
  private String toRgbString(Color color) {
    return "rgb(" + (int) (color.getRed() * 255) + "," + (int) (color.getGreen() * 255) + "," + (int) (color.getBlue() * 255) + ")";
  }

  /**
   * Open a file chooser to select and import a CSV file.
   */
  private void importCSV() {
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
  private void toggleBold() {
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
  private void toggleItalic() {
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

  // Class representing an edit action for undo/redo functionality
  private class EditAction {
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
    private void apply(String value) {
      Main mainApp = (Main) tableView.getScene().getWindow().getUserData();
      String cellKey = mainApp.getCellKey(row, column);
      switch (actionType) {
        case VALUE_CHANGE:
          row.get(column).set(value);
          break;
        case ALIGNMENT_CHANGE:
          mainApp.alignments.put(cellKey, Pos.valueOf(value));
          mainApp.tableView.refresh();
          break;
        case FONT_SIZE_CHANGE:
          mainApp.fontSizes.put(cellKey, value);
          mainApp.updateCellStyle(cellKey);
          mainApp.tableView.refresh();
          break;
        case TEXT_COLOR_CHANGE:
          mainApp.textColors.put(cellKey, value);
          mainApp.updateCellStyle(cellKey);
          mainApp.tableView.refresh();
          break;
        case BACKGROUND_COLOR_CHANGE:
          mainApp.backgroundColors.put(cellKey, value);
          mainApp.updateCellStyle(cellKey);
          mainApp.tableView.refresh();
          break;
        case FONT_CHANGE:
          mainApp.fonts.put(cellKey, value);
          mainApp.updateCellStyle(cellKey);
          mainApp.tableView.refresh();
          break;
      }
    }
  }

  // Custom TableCell class to support cell alignment
  private class AlignedTextFieldTableCell extends TextFieldTableCell<ObservableList<SimpleStringProperty>, String> {
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

  @Override
  public void stop() throws Exception {
    scheduler.shutdown();
    super.stop();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
