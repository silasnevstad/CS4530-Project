package com.example.huskysheets2;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.converter.DefaultStringConverter;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class Main extends Application {

  // Constants for the number of rows and columns
  private static final int NUM_ROWS = 100;
  private static final int NUM_COLUMNS = 26;
  private TableView<ObservableList<SimpleStringProperty>> tableView;
  private TextField formulaField;
  private TextField titleField;
  private Stack<EditAction> undoStack = new Stack<>();
  private Stack<EditAction> redoStack = new Stack<>();
  private String clipboardContent = "";
  private Map<String, String> formulas = new HashMap<>();
  private Map<String, Pos> alignments = new HashMap<>();
  private Map<String, String> fontSizes = new HashMap<>();
  private Map<String, String> textColors = new HashMap<>();
  private Map<String, String> backgroundColors = new HashMap<>();
  private Map<String, String> fonts = new HashMap<>();
  private Map<String, String> styles = new HashMap<>();

  @Override
  public void start(Stage primaryStage) {
    BorderPane root = new BorderPane();

    // Initialize the table view
    tableView = new TableView<>();
    tableView.setEditable(true);

    // Create columns
    for (int col = 0; col <= NUM_COLUMNS; col++) {
      TableColumn<ObservableList<SimpleStringProperty>, String> column;
      if (col == 0) {
        // Create row number column
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
          evaluateCell(event.getRowValue(), colIndex);
        });
        column.setPrefWidth(100);
      }
      tableView.getColumns().add(column);
    }

    // Create data rows
    ObservableList<ObservableList<SimpleStringProperty>> data = FXCollections.observableArrayList();
    for (int row = 0; row < NUM_ROWS; row++) {
      ObservableList<SimpleStringProperty> rowData = FXCollections.observableArrayList();
      for (int col = 0; col < NUM_COLUMNS; col++) {
        rowData.add(new SimpleStringProperty(""));
      }
      data.add(rowData);
    }
    tableView.setItems(data);

    // Initialize formula field for editing cell values
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
        evaluateCell(row, selectedCell.getColumn() - 1);
      }
    });

    // Update the formula field when a new cell is selected
    tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
      if (newSelection != null && !tableView.getSelectionModel().getSelectedCells().isEmpty()) {
        TablePosition selectedCell = tableView.getSelectionModel().getSelectedCells().get(0);
        String cellKey = getCellKey(newSelection, selectedCell.getColumn() - 1);
        formulaField.setText(formulas.getOrDefault(cellKey, ""));
      }
    });

    VBox topContainer = new VBox();

    // Create title field for the spreadsheet
    titleField = new TextField("Untitled Spreadsheet");
    titleField.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
    titleField.setEditable(true);
    titleField.setPrefWidth(400);
    HBox titleBox = new HBox(titleField);
    titleBox.setStyle("-fx-padding: 10px;");

    // Create toolbar with various buttons and controls
    ToolBar toolBar = new ToolBar();
    Button undoButton = new Button("Undo");
    Button redoButton = new Button("Redo");
    Button cutButton = new Button("Cut");
    Button copyButton = new Button("Copy");
    Button pasteButton = new Button("Paste");
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

    // Set actions for the buttons
    undoButton.setOnAction(e -> undo());
    redoButton.setOnAction(e -> redo());
    cutButton.setOnAction(e -> cut());
    copyButton.setOnAction(e -> copy());
    pasteButton.setOnAction(e -> paste());

    toolBar.getItems().addAll(undoButton, redoButton, cutButton, copyButton, pasteButton,
        new Label("Font:"), fontComboBox, new Label("Size:"), fontSizeField,
        new Label("Text Color:"), textColorPicker, new Label("Background Color:"), backgroundColorPicker);

    topContainer.getChildren().addAll(titleBox, toolBar, formulaField);

    root.setTop(topContainer);
    root.setCenter(tableView);

    Scene scene = new Scene(root, 800, 600);
    primaryStage.setScene(scene);
    primaryStage.setTitle("Excel-Like Spreadsheet");
    primaryStage.show();

    // Set user data for access in other methods
    primaryStage.setUserData(this);
  }

  // Method to generate column headers (A, B, C, ..., Z, AA, AB, etc.)
  private String getColumnHeader(int index) {
    StringBuilder sb = new StringBuilder();
    while (index >= 0) {
      sb.insert(0, (char) ('A' + (index % 26)));
      index = (index / 26) - 1;
    }
    return sb.toString();
  }

  // Method to undo the last action
  private void undo() {
    if (!undoStack.isEmpty()) {
      EditAction action = undoStack.pop();
      action.undo();
      redoStack.push(action);
    }
  }

  // Method to redo the last undone action
  private void redo() {
    if (!redoStack.isEmpty()) {
      EditAction action = redoStack.pop();
      action.redo();
      undoStack.push(action);
    }
  }

  // Method to cut the selected cell's content
  private void cut() {
    if (!tableView.getSelectionModel().getSelectedCells().isEmpty()) {
      TablePosition selectedCell = tableView.getSelectionModel().getSelectedCells().get(0);
      ObservableList<SimpleStringProperty> row = tableView.getItems().get(selectedCell.getRow());
      clipboardContent = row.get(selectedCell.getColumn() - 1).get();
      undoStack.push(new EditAction(row, selectedCell.getColumn() - 1, clipboardContent, "", EditAction.ActionType.VALUE_CHANGE));
      row.get(selectedCell.getColumn() - 1).set("");
      redoStack.clear();
    }
  }

  // Method to copy the selected cell's content
  private void copy() {
    if (!tableView.getSelectionModel().getSelectedCells().isEmpty()) {
      TablePosition selectedCell = tableView.getSelectionModel().getSelectedCells().get(0);
      ObservableList<SimpleStringProperty> row = tableView.getItems().get(selectedCell.getRow());
      clipboardContent = row.get(selectedCell.getColumn() - 1).get();
    }
  }

  // Method to paste content into the selected cell
  private void paste() {
    if (!tableView.getSelectionModel().getSelectedCells().isEmpty()) {
      TablePosition selectedCell = tableView.getSelectionModel().getSelectedCells().get(0);
      ObservableList<SimpleStringProperty> row = tableView.getItems().get(selectedCell.getRow());
      String oldValue = row.get(selectedCell.getColumn() - 1).get();
      undoStack.push(new EditAction(row, selectedCell.getColumn() - 1, oldValue, clipboardContent, EditAction.ActionType.VALUE_CHANGE));
      row.get(selectedCell.getColumn() - 1).set(clipboardContent);
      redoStack.clear();
      String cellKey = getCellKey(row, selectedCell.getColumn() - 1);
      formulas.put(cellKey, clipboardContent);
      evaluateCell(row, selectedCell.getColumn() - 1);
    }
  }

  // Method to evaluate a cell's content as a formula
  private void evaluateCell(ObservableList<SimpleStringProperty> row, int column) {
    String cellKey = getCellKey(row, column);
    String formula = formulas.getOrDefault(cellKey, "");
    if (formula.isEmpty()) {
      row.get(column).set("");
      return;
    }
    if (!formula.startsWith("=")) {
      row.get(column).set(formula);
      return;
    }
    try {
      double result = ArithmeticParser.evaluate(formula.substring(1));
      row.get(column).set(String.valueOf(result));
    } catch (Exception e) {
      row.get(column).set("#REF!");
    }
  }

  // Method to generate a unique key for each cell based on its row and column
  private String getCellKey(ObservableList<SimpleStringProperty> row, int column) {
    int rowIndex = tableView.getItems().indexOf(row);
    return rowIndex + "," + column;
  }

  // Method to change the font of the selected cell
  private void changeFont(String font) {
    if (!tableView.getSelectionModel().getSelectedCells().isEmpty()) {
      TablePosition selectedCell = tableView.getSelectionModel().getSelectedCells().get(0);
      ObservableList<SimpleStringProperty> row = tableView.getItems().get(selectedCell.getRow());
      String cellKey = getCellKey(row, selectedCell.getColumn() - 1);
      undoStack.push(new EditAction(row, selectedCell.getColumn() - 1, fonts.get(cellKey), font, EditAction.ActionType.FONT_CHANGE));
      fonts.put(cellKey, font);
      updateCellStyle(cellKey);
      redoStack.clear();
    }
  }

  // Method to change the font size of the selected cell
  private void changeFontSize(String fontSize) {
    if (!tableView.getSelectionModel().getSelectedCells().isEmpty()) {
      TablePosition selectedCell = tableView.getSelectionModel().getSelectedCells().get(0);
      ObservableList<SimpleStringProperty> row = tableView.getItems().get(selectedCell.getRow());
      String cellKey = getCellKey(row, selectedCell.getColumn() - 1);
      undoStack.push(new EditAction(row, selectedCell.getColumn() - 1, fontSizes.get(cellKey), fontSize, EditAction.ActionType.FONT_SIZE_CHANGE));
      fontSizes.put(cellKey, fontSize);
      updateCellStyle(cellKey);
      redoStack.clear();
    }
  }

  // Method to change the text color of the selected cell
  private void changeTextColor(Color color) {
    if (!tableView.getSelectionModel().getSelectedCells().isEmpty()) {
      TablePosition selectedCell = tableView.getSelectionModel().getSelectedCells().get(0);
      ObservableList<SimpleStringProperty> row = tableView.getItems().get(selectedCell.getRow());
      String cellKey = getCellKey(row, selectedCell.getColumn() - 1);
      String colorString = toRgbString(color);
      undoStack.push(new EditAction(row, selectedCell.getColumn() - 1, textColors.get(cellKey), colorString, EditAction.ActionType.TEXT_COLOR_CHANGE));
      textColors.put(cellKey, colorString);
      updateCellStyle(cellKey);
      redoStack.clear();
    }
  }

  // Method to change the background color of the selected cell
  private void changeBackgroundColor(Color color) {
    if (!tableView.getSelectionModel().getSelectedCells().isEmpty()) {
      TablePosition selectedCell = tableView.getSelectionModel().getSelectedCells().get(0);
      ObservableList<SimpleStringProperty> row = tableView.getItems().get(selectedCell.getRow());
      String cellKey = getCellKey(row, selectedCell.getColumn() - 1);
      String colorString = toRgbString(color);
      undoStack.push(new EditAction(row, selectedCell.getColumn() - 1, backgroundColors.get(cellKey), colorString, EditAction.ActionType.BACKGROUND_COLOR_CHANGE));
      backgroundColors.put(cellKey, colorString);
      updateCellStyle(cellKey);
      redoStack.clear();
    }
  }

  // Method to update the style of a cell based on its attributes
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
    styles.put(cellKey, style.toString());
    tableView.refresh();
  }

  // Method to convert a Color object to an RGB string
  private String toRgbString(Color color) {
    return "rgb(" + (int) (color.getRed() * 255) + "," + (int) (color.getGreen() * 255) + "," + (int) (color.getBlue() * 255) + ")";
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
        String cellKey = getCellKey(getTableRow().getItem(), getTableView().getColumns().indexOf(getTableColumn()) - 1);
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

  public static void main(String[] args) {
    launch(args);
  }
}
