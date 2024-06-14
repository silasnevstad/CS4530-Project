// Owner: Zach Pulichino
package com.group12.husksheets.ui;

import com.group12.husksheets.ui.controllers.SheetView;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class SheetViewTest {

  private SheetView sheetView;
  private Stage stage;

  // Initialize the JavaFX toolkit before running any tests
  @BeforeAll
  public static void initToolkit() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    Platform.startup(() -> {
      latch.countDown();
    });
    latch.await(5, TimeUnit.SECONDS); // Wait for JavaFX toolkit to initialize
  }

  // Setup method to initialize the SheetView and Stage before each test
  @BeforeEach
  public void setup() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    Platform.runLater(() -> {
      try {
        sheetView = new SheetView();
        stage = new Stage();
        sheetView.start(stage); // Start the application
        latch.countDown();
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
    latch.await(5, TimeUnit.SECONDS); // Wait for the JavaFX initialization to complete
  }

  // Test editing a cell's value
  @Test
  public void testEditCellValue() {
    Platform.runLater(() -> {
      ObservableList<SimpleStringProperty> row = sheetView.tableView.getItems().get(0);
      row.get(0).set("New Value");
      assertEquals("New Value", row.get(0).get());
    });
  }

  // Test the undo and redo functionality
  @Test
  public void testUndoRedo() {
    Platform.runLater(() -> {
      ObservableList<SimpleStringProperty> row = sheetView.tableView.getItems().get(0);
      row.get(0).set("First Value");
      row.get(0).set("Second Value");

      sheetView.undo();
      assertEquals("First Value", row.get(0).get());

      sheetView.redo();
      assertEquals("Second Value", row.get(0).get());
    });
  }

  // Test cut, copy, and paste functionality
  @Test
  public void testCutCopyPaste() {
    Platform.runLater(() -> {
      ObservableList<SimpleStringProperty> row = sheetView.tableView.getItems().get(0);
      row.get(0).set("CutCopyTest");

      sheetView.cut();
      assertEquals("", row.get(0).get());
      assertEquals("CutCopyTest", sheetView.clipboard.getString());

      row.get(1).set("");
      sheetView.paste();
      assertEquals("CutCopyTest", row.get(1).get());
    });
  }

  // Test changing the font of the selected cell
  @Test
  public void testChangeFont() {
    Platform.runLater(() -> {
      sheetView.tableView.getSelectionModel().select(0);
      sheetView.changeFont("Arial");
      ObservableList<SimpleStringProperty> row = sheetView.tableView.getItems().get(0);
      String cellKey = sheetView.getCellKey(row, 0);
      assertEquals("Arial", sheetView.fonts.get(cellKey));
    });
  }

  // Test changing the font size of the selected cell
  @Test
  public void testChangeFontSize() {
    Platform.runLater(() -> {
      sheetView.tableView.getSelectionModel().select(0);
      sheetView.changeFontSize("16");
      ObservableList<SimpleStringProperty> row = sheetView.tableView.getItems().get(0);
      String cellKey = sheetView.getCellKey(row, 0);
      assertEquals("16", sheetView.fontSizes.get(cellKey));
    });
  }

  // Test changing the text color of the selected cell
  @Test
  public void testChangeTextColor() {
    Platform.runLater(() -> {
      sheetView.tableView.getSelectionModel().select(0);
      sheetView.changeTextColor(Color.RED);
      ObservableList<SimpleStringProperty> row = sheetView.tableView.getItems().get(0);
      String cellKey = sheetView.getCellKey(row, 0);
      assertEquals("rgb(255,0,0)", sheetView.textColors.get(cellKey));
    });
  }

  // Test changing the background color of the selected cell
  @Test
  public void testChangeBackgroundColor() {
    Platform.runLater(() -> {
      sheetView.tableView.getSelectionModel().select(0);
      sheetView.changeBackgroundColor(Color.GREEN);
      ObservableList<SimpleStringProperty> row = sheetView.tableView.getItems().get(0);
      String cellKey = sheetView.getCellKey(row, 0);
      assertEquals("rgb(0,255,0)", sheetView.backgroundColors.get(cellKey));
    });
  }

  // Test toggling the bold style for the selected cell
  @Test
  public void testToggleBold() {
    Platform.runLater(() -> {
      sheetView.tableView.getSelectionModel().select(0);
      sheetView.toggleBold();
      ObservableList<SimpleStringProperty> row = sheetView.tableView.getItems().get(0);
      String cellKey = sheetView.getCellKey(row, 0);
      assertTrue(sheetView.boldStyles.get(cellKey));

      sheetView.toggleBold();
      assertFalse(sheetView.boldStyles.get(cellKey));
    });
  }

  // Test toggling the italic style for the selected cell
  @Test
  public void testToggleItalic() {
    Platform.runLater(() -> {
      sheetView.tableView.getSelectionModel().select(0);
      sheetView.toggleItalic();
      ObservableList<SimpleStringProperty> row = sheetView.tableView.getItems().get(0);
      String cellKey = sheetView.getCellKey(row, 0);
      assertTrue(sheetView.italicStyles.get(cellKey));

      sheetView.toggleItalic();
      assertFalse(sheetView.italicStyles.get(cellKey));
    });
  }

  // Test evaluating a formula in a cell
  @Test
  public void testEvaluateCell() {
    Platform.runLater(() -> {
      ObservableList<SimpleStringProperty> row = sheetView.tableView.getItems().get(0);
      sheetView.formulas.put(sheetView.getCellKey(row, 0), "=1+1");
      sheetView.evaluateCell(row, 0);
      assertEquals("2", row.get(0).get());
    });
  }

  // Test generating column headers
  @Test
  public void testGetColumnHeader() {
    assertEquals("A", sheetView.getColumnHeader(0));
    assertEquals("Z", sheetView.getColumnHeader(25));
    assertEquals("AA", sheetView.getColumnHeader(26));
  }

  // Test generating a unique key for a cell
  @Test
  public void testGetCellKey() {
    ObservableList<SimpleStringProperty> row = sheetView.tableView.getItems().get(0);
    assertEquals("0,0", sheetView.getCellKey(row, 0));
  }

  // Test converting a Color object to an RGB string
  @Test
  public void testToRgbString() {
    Color color = Color.rgb(255, 100, 50);
    assertEquals("rgb(255,100,50)", sheetView.toRgbString(color));
  }

  // Placeholder for testing the CSV import functionality
  @Test
  public void testImportCSV() {
    // Implement the test by simulating a CSV file import.
  }
}
