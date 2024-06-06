package org.example;


import javafx.collections.ObservableList;
import javafx.scene.control.TableView;

public class FormulaParser {
    private final TableView<ObservableList<String>> tableView;

    public FormulaParser(TableView<ObservableList<String>> tableView) {
        this.tableView = tableView;
    }

    public String evaluateFormula(String formula) {
        if (formula.startsWith("=SUM(") && formula.endsWith(")")) {
            String cells = formula.substring(5, formula.length() - 1);
            String[] cellRefs = cells.split(",");
            if (cellRefs.length >= 1) {
                double result = SpreadsheetUtils.sum(tableView, cellRefs);
                return String.valueOf(result);
            }
        }
        if (formula.startsWith("=MIN(") && formula.endsWith(")")) {
            String cells = formula.substring(5, formula.length() - 1);
            String[] cellRefs = cells.split(",");
            if (cellRefs.length >= 1) {
                double result = SpreadsheetUtils.min(tableView, cellRefs);
                return String.valueOf(result);
            }
        }
        if (formula.startsWith("=MAX(") && formula.endsWith(")")) {
            String cells = formula.substring(5, formula.length() - 1);
            String[] cellRefs = cells.split(",");
            if (cellRefs.length >= 1) {
                double result = SpreadsheetUtils.max(tableView, cellRefs);
                return String.valueOf(result);
            }
        }
        return "ERROR";
    }
}
