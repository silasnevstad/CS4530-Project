package com.example.husksheets_jktesting;

import javafx.collections.ObservableList;
import javafx.scene.control.TableView;

public class FormulaParser {
    private TableView<ObservableList<String>> tableView;

    public FormulaParser(TableView<ObservableList<String>> tableView) {
        this.tableView = tableView;
    }

    public String evaluateFormula(String formula) {
        if (formula.startsWith("=SUM(") && formula.endsWith(")")) {
            System.out.println("SUM Detected");
            System.out.println("formula is : " + formula);
            // Extract the cell references
            String cells = formula.substring(5, formula.length() - 1);
            System.out.println("cells is : " + cells);
            String[] cellRefs = cells.split(",");
            if (cellRefs.length >= 1) {
                double result = SpreadsheetUtils.sum(tableView, cellRefs);
                return String.valueOf(result);
            }
        }
        return "ERROR";
    }
}
