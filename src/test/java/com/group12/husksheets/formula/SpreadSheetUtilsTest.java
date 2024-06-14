package com.group12.husksheets.formula;

// Owner: Jason King and Silas Nevstad

import com.group12.husksheets.ui.utils.SpreadsheetUtils;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import static org.junit.jupiter.api.Assertions.*;

class SpreadsheetUtilsTest extends ApplicationTest {

    private TableView<ObservableList<SimpleStringProperty>> tableView;

    @BeforeEach
    void setUp() {
        Platform.runLater(() -> {
            tableView = new TableView<>();
            for (int i = 0; i < 5; i++) {
                ObservableList<SimpleStringProperty> row = FXCollections.observableArrayList();
                for (int j = 0; j < 5; j++) {
                    row.add(new SimpleStringProperty(i * j == 0 ? "0" : String.valueOf(i * j)));
                }
                tableView.getItems().add(row);
            }
        });
    }


    @Disabled
    @Test
    void testSumRange() {
        Platform.runLater(() -> {
            double sum = SpreadsheetUtils.sumRange(tableView, "A1:B2");
            assertEquals(0, sum);
        });
    }

    @Test
    void testSumSingleCell() {
        Platform.runLater(() -> {
            double sum = SpreadsheetUtils.sum(tableView, "A2");
            assertEquals(0, sum);
        });
    }

    @Test
    void testMinRange() {
        Platform.runLater(() -> {
            double min = SpreadsheetUtils.minRange(tableView, "A1:C3");
            assertEquals(0, min);
        });
    }

    @Disabled
    @Test
    void testMinSingleCell() {
        Platform.runLater(() -> {
            double min = SpreadsheetUtils.min(tableView, "B3");
            assertEquals(0, min);
        });
    }

    @Disabled
    @Test
    void testMaxRange() {
        Platform.runLater(() -> {
            double max = SpreadsheetUtils.maxRange(tableView, "A1:D4");
            assertEquals(16, max);
        });
    }

    @Disabled
    @Test
    void testMaxSingleCell() {
        Platform.runLater(() -> {
            double max = SpreadsheetUtils.max(tableView, "D4");
            assertEquals(16, max);
        });
    }

    @Disabled
    @Test
    void testConcatRange() {
        Platform.runLater(() -> {
            String concat = SpreadsheetUtils.concatRange(tableView, "A1:B2");
            assertEquals("0000", concat);
        });
    }

    @Test
    void testConcatSingleCell() {
        Platform.runLater(() -> {
            String concat = SpreadsheetUtils.concat(tableView, "A2");
            assertEquals("0", concat);
        });
    }

    @Disabled
    @Test
    void testAvgRange() {
        Platform.runLater(() -> {
            double[] avg = SpreadsheetUtils.avgRange(tableView, "A1:C3");
            assertEquals(0, avg[0]); // sum
            assertEquals(9, avg[1]); // count
        });
    }

    @Disabled
    @Test
    void testIfFunctionTrue() {
        Platform.runLater(() -> {
            String result = SpreadsheetUtils.ifFunction(tableView, "A1==0", "True", "False");
            assertEquals("True", result);
        });
    }

    @Disabled
    @Test
    void testIfFunctionFalse() {
        Platform.runLater(() -> {
            String result = SpreadsheetUtils.ifFunction(tableView, "A1!=0", "True", "False");
            assertEquals("False", result);
        });
    }

    @Test
    void testDebugFunction() {
        Platform.runLater(() -> {
            String result = SpreadsheetUtils.debugFunction(tableView, "\"Debug\"");
            assertEquals("Debug", result);
        });
    }
}
