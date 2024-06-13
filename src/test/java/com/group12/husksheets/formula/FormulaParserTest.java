package com.group12.husksheets.formula;


import com.group12.husksheets.ui.utils.FormulaParser;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FormulaParserTest {

    private TableView<ObservableList<SimpleStringProperty>> tableView;
    private FormulaParser formulaParser;

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
        formulaParser = new FormulaParser(tableView);
    }

    @Test
    void testEvaluateSumFormula() {
        String result = formulaParser.evaluateFormula("=SUM(A1,B2)");
        assertEquals("0.0", result);
    }

    @Test
    void testEvaluateMinFormula() {
        String result = formulaParser.evaluateFormula("=MIN(A1:C3)");
        assertEquals("0.0", result);
    }

    @Test
    void testEvaluateMaxFormula() {
        String result = formulaParser.evaluateFormula("=MAX(A1:D4)");
        assertEquals("16.0", result);
    }

    @Test
    void testEvaluateConcatFormula() {
        String result = formulaParser.evaluateFormula("=CONCAT(A1,B2)");
        assertEquals("00", result);
    }

    @Test
    void testEvaluateIfFormulaTrue() {
        String result = formulaParser.evaluateFormula("=IF(A1==0,\"True\",\"False\")");
        assertEquals("True", result);
    }

    @Test
    void testEvaluateIfFormulaFalse() {
        String result = formulaParser.evaluateFormula("=IF(A1!=0,\"True\",\"False\")");
        assertEquals("False", result);
    }

    @Test
    void testEvaluateDebugFormula() {
        String result = formulaParser.evaluateFormula("=DEBUG(\"Debug\")");
        assertEquals("Debug", result);
    }

    @Test
    void testEvaluateArithmeticExpression() {
        String result = formulaParser.evaluateFormula("=1+2*3-4/2");
        assertEquals("5.0", result);
    }

    @Test
    void testInvalidFormula() {
        String result = formulaParser.evaluateFormula("=INVALID(A1)");
        assertEquals("#REF!", result);
    }

    @Test
    void testParseCellReference() {
        int[] cellRef = FormulaParser.parseCellReference("A1");
        assertArrayEquals(new int[]{0, 0}, cellRef);
    }

    @Test
    void testIsNumeric() {
        assertTrue(FormulaParser.isNumeric("123"));
        assertFalse(FormulaParser.isNumeric("abc"));
    }
}
