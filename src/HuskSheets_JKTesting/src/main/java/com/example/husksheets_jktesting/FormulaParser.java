package com.example.husksheets_jktesting;

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
            double result = 0;
            for (String cellRef : cellRefs) {
                if (cellRef.contains(":")) {
                    result += SpreadsheetUtils.sumRange(tableView, cellRef);
                } else {
                    result += SpreadsheetUtils.sum(tableView, cellRef);
                }
            }
            return String.valueOf(result);
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
        if (formula.startsWith("=CONCAT(") && formula.endsWith(")")) {
            String cells = formula.substring(8, formula.length() - 1);
            String[] cellRefs = cells.split(",");
            StringBuilder result = new StringBuilder();
            for (String cellRef : cellRefs) {
                if (cellRef.contains(":")) {
                    result.append(SpreadsheetUtils.concatRange(tableView, cellRef));
                } else {
                    result.append(SpreadsheetUtils.concat(tableView, cellRef));
                }
            }
            return result.toString();
        }
        if (formula.startsWith("=IF(") && formula.endsWith(")")) {
            String[] parts = formula.substring(4, formula.length() - 1).split(",", 3);
            if (parts.length == 3) {
                return SpreadsheetUtils.ifFunction(tableView, parts[0].trim(), parts[1].trim(), parts[2].trim());
            } else {
                return "ERROR";
            }
        }
        if (formula.startsWith("=DEBUG(") && formula.endsWith(")")) {
            String expression = formula.substring(7, formula.length() - 1);
            return SpreadsheetUtils.debugFunction(tableView, expression.trim());
        }
        return "ERROR";
    }
}
