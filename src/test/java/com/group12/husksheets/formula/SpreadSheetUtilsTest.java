package com.group12.husksheets.formula;




import com.group12.husksheets.ui.utils.SpreadsheetUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import javafx.scene.control.TableView;

import static org.junit.jupiter.api.Assertions.*;

//Owner:Jason King

class SpreadsheetUtilsTest {

    private TableView<ObservableList<SimpleStringProperty>> tableView;

    @BeforeEach
    void setUp() {
        tableView = new TableView<>();
        for (int i = 0; i < 5; i++) {
            ObservableList<SimpleStringProperty> row = FXCollections.observableArrayList();
            for (int j = 0; j < 5; j++) {
                row.add(new SimpleStringProperty(i * j == 0 ? "0" : String.valueOf(i * j)));
            }
            tableView.getItems().add(row);
        }
    }

    @Test
    void testSumRange() {
        double sum = SpreadsheetUtils.sumRange(tableView, "A1:B2");
        assertEquals(0, sum);
    }

    @Test
    void testSumSingleCell() {
        double sum = SpreadsheetUtils.sum(tableView, "A2");
        assertEquals(0, sum);
    }

    @Test
    void testMinRange() {
        double min = SpreadsheetUtils.minRange(tableView, "A1:C3");
        assertEquals(0, min);
    }

    @Test
    void testMinSingleCell() {
        double min = SpreadsheetUtils.min(tableView, "B3");
        assertEquals(0, min);
    }

    @Test
    void testMaxRange() {
        double max = SpreadsheetUtils.maxRange(tableView, "A1:D4");
        assertEquals(16, max);
    }

    @Test
    void testMaxSingleCell() {
        double max = SpreadsheetUtils.max(tableView, "D4");
        assertEquals(16, max);
    }

    @Test
    void testConcatRange() {
        String concat = SpreadsheetUtils.concatRange(tableView, "A1:B2");
        assertEquals("0000", concat);
    }

    @Test
    void testConcatSingleCell() {
        String concat = SpreadsheetUtils.concat(tableView, "A2");
        assertEquals("0", concat);
    }

    @Test
    void testAvgRange() {
        double[] avg = SpreadsheetUtils.avgRange(tableView, "A1:C3");
        assertEquals(0, avg[0]); // sum
        assertEquals(9, avg[1]); // count
    }

    @Test
    void testIfFunctionTrue() {
        String result = SpreadsheetUtils.ifFunction(tableView, "A1==0", "True", "False");
        assertEquals("True", result);
    }

    @Test
    void testIfFunctionFalse() {
        String result = SpreadsheetUtils.ifFunction(tableView, "A1!=0", "True", "False");
        assertEquals("False", result);
    }

    @Test
    void testDebugFunction() {
        String result = SpreadsheetUtils.debugFunction(tableView, "\"Debug\"");
        assertEquals("Debug", result);
    }

    //Ask how test private expressions
    /*
    @Test
    void testEvaluateConditionEquals() {
        boolean result = SpreadsheetUtils.evaluateCondition(tableView, "A1==0");
        assertTrue(result);
    }

    @Test
    void testEvaluateConditionNotEquals() {
        boolean result = SpreadsheetUtils.evaluateCondition(tableView, "A1!=1");
        assertTrue(result);
    }

    @Test
    void testEvaluateConditionLessThan() {
        boolean result = SpreadsheetUtils.evaluateCondition(tableView, "A1<1");
        assertTrue(result);
    }

    @Test
    void testEvaluateConditionGreaterThan() {
        boolean result = SpreadsheetUtils.evaluateCondition(tableView, "A1>0");
        assertFalse(result);
    }

    @Test
    void testEvaluateExpressionString() {
        String result = SpreadsheetUtils.evaluateExpression(tableView, "\"String\"");
        assertEquals("String", result);
    }

    @Test
    void testEvaluateExpressionNumber() {
        String result = SpreadsheetUtils.evaluateExpression(tableView, "123");
        assertEquals("123", result);
    }

    @Test
    void testEvaluateExpressionCell() {
        String result = SpreadsheetUtils.evaluateExpression(tableView, "A1");
        assertEquals("0", result);
    }
    */

}
