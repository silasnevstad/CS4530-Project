package com.group12.husksheets.formula;

import com.group12.husksheets.ui.controllers.SheetView;
import com.group12.husksheets.ui.utils.ColumnNameUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/*
import org.testfx.framework.junit5.ApplicationTest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SheetViewTest extends ApplicationTest {

    private SheetView sheetView;
    private TableView<ObservableList<SimpleStringProperty>> tableView;

    @BeforeEach
    void setUp() {
        sheetView = new SheetView();
        tableView = new TableView<>();
        sheetView.tableView = tableView;

        // Initialize the TableView with some rows and columns
        for (int col = 0; col < 3; col++) {
            tableView.getColumns().add(new TableColumn<>(ColumnNameUtils.getColumnName(col)));
        }

        for (int row = 0; row < 3; row++) {
            ObservableList<SimpleStringProperty> rowData = FXCollections.observableArrayList();
            for (int col = 0; col < 3; col++) {
                rowData.add(new SimpleStringProperty(""));
            }
            tableView.getItems().add(rowData);
        }
    }

    @Test
    void testGetCellKey() {
        ObservableList<SimpleStringProperty> row = tableView.getItems().get(1);
        String cellKey = sheetView.getCellKey(row, 2);
        assertEquals("1,2", cellKey);
    }

    @Test
    void testUpdateCellStyle() {
        ObservableList<SimpleStringProperty> row = tableView.getItems().get(1);
        String cellKey = sheetView.getCellKey(row, 1);
        sheetView.textColors.put(cellKey, "rgb(255,0,0)");
        sheetView.updateCellStyle(cellKey);
        assertTrue(sheetView.styles.get(cellKey).contains("-fx-text-fill: rgb(255,0,0);"));
    }

    @Test
    void testEvaluateCell() {
        ObservableList<SimpleStringProperty> row = tableView.getItems().get(1);
        row.get(1).set("=2+2");
        sheetView.formulas.put(sheetView.getCellKey(row, 1), "=2+2");
        sheetView.evaluateCell(row, 1);
        assertEquals("4.0", row.get(1).get());
    }

    @Test
    void testToRgbString() {
        String rgbString = sheetView.toRgbString(Color.rgb(255, 0, 0));
        assertEquals("rgb(255,0,0)", rgbString);
    }

    @Test
    void testToggleBold() {
        // Set up a scenario to toggle bold style
        TablePosition selectedCell = new TablePosition(tableView, 1, 1);
        tableView.getSelectionModel().select(selectedCell.getRow(), selectedCell.getTableColumn());
        sheetView.boldStyles.put(sheetView.getCellKey(tableView.getItems().get(selectedCell.getRow()), selectedCell.getColumn()), false);
        sheetView.toggleBold();
        assertTrue(sheetView.boldStyles.get(sheetView.getCellKey(tableView.getItems().get(selectedCell.getRow()), selectedCell.getColumn())));
    }

    @Test
    void testToggleItalic() {
        // Set up a scenario to toggle italic style
        TablePosition selectedCell = new TablePosition(tableView, 1, 1);
        tableView.getSelectionModel().select(selectedCell.getRow(), selectedCell.getTableColumn());
        sheetView.italicStyles.put(sheetView.getCellKey(tableView.getItems().get(selectedCell.getRow()), selectedCell.getColumn()), false);
        sheetView.toggleItalic();
        assertTrue(sheetView.italicStyles.get(sheetView.getCellKey(tableView.getItems().get(selectedCell.getRow()), selectedCell.getColumn())));
    }

    // Add more tests for other methods as needed...
}
*/