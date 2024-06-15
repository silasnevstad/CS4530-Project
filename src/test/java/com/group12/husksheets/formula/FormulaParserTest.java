package com.group12.husksheets.formula;


//Owner:Jason King and Silas Nevstad
import com.group12.husksheets.ui.utils.FormulaParser;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import org.testfx.framework.junit5.ApplicationTest;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FormulaParserTest extends ApplicationTest {

    private TableView<ObservableList<SimpleStringProperty>> tableView;
    private FormulaParser formulaParser;

    @Override
    public void start(Stage stage) {
        tableView = new TableView<>();
        Scene scene = new Scene(tableView);
        stage.setScene(scene);
        stage.show();
    }

    @BeforeEach
    void setUp() {
        Platform.runLater(() -> {
            for (int i = 0; i < 5; i++) {
                ObservableList<SimpleStringProperty> row = FXCollections.observableArrayList();
                for (int j = 0; j < 5; j++) {
                    row.add(new SimpleStringProperty(String.valueOf(i * j)));
                }
                tableView.getItems().add(row);
            }
            formulaParser = new FormulaParser(tableView);
        });
        waitForFxEvents();
    }

    private void waitForFxEvents() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testEvaluateSumFormula() {
        Platform.runLater(() -> {
            String result = formulaParser.evaluateFormula("=SUM(A1,B2)");
            assertEquals("1.0", result);
        });
        waitForFxEvents();
    }

    @Test
    void testEvaluateMinFormula() {
        Platform.runLater(() -> {
            String result = formulaParser.evaluateFormula("=MIN(A1:C3)");
            assertEquals("0.0", result);
        });
        waitForFxEvents();
    }

    @Test
    void testEvaluateMaxFormula() {
        Platform.runLater(() -> {
            String result = formulaParser.evaluateFormula("=MAX(A1:D4)");
            assertEquals("9.0", result);
        });
        waitForFxEvents();
    }

    @Test
    void testEvaluateConcatFormula() {
        Platform.runLater(() -> {
            String result = formulaParser.evaluateFormula("=CONCAT(A1,B2)");
            assertEquals("01", result);
        });
        waitForFxEvents();
    }

    @Test
    void testEvaluateIfFormulaTrue() {
        Platform.runLater(() -> {
            String result = formulaParser.evaluateFormula("=IF(A1==0,\"True\",\"False\")");
            assertEquals("True", result);
        });
        waitForFxEvents();
    }

    @Test
    void testEvaluateIfFormulaFalse() {
        Platform.runLater(() -> {
            String result = formulaParser.evaluateFormula("=IF(A1!=0,\"True\",\"False\")");
            assertEquals("False", result);
        });
        waitForFxEvents();
    }

    @Test
    void testEvaluateDebugFormula() {
        Platform.runLater(() -> {
            String result = formulaParser.evaluateFormula("=DEBUG(\"Debug\")");
            assertEquals("Debug", result);
        });
        waitForFxEvents();
    }

    @Test
    void testEvaluateArithmeticExpression() {
        Platform.runLater(() -> {
            String result = formulaParser.evaluateFormula("=1+2*3-4/2");
            assertEquals("5.0", result);
        });
        waitForFxEvents();
    }

    @Test
    void testInvalidFormula() {
        Platform.runLater(() -> {
            String result = formulaParser.evaluateFormula("=INVALID(A1)");
            assertEquals("#REF!", result);
        });
        waitForFxEvents();
    }

    @Test
    void testParseCellReference() {
        Platform.runLater(() -> {
            int[] cellRef = FormulaParser.parseCellReference("A1");
            assertArrayEquals(new int[]{0, 0}, cellRef);
        });
        waitForFxEvents();
    }

    @Test
    void testIsNumeric() {
        Platform.runLater(() -> {
            assertTrue(FormulaParser.isNumeric("123"));
            assertFalse(FormulaParser.isNumeric("abc"));
        });
        waitForFxEvents();
    }
}
