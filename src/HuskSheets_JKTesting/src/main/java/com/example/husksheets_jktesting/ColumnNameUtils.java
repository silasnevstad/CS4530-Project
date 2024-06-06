package com.example.husksheets_jktesting;

/**
 * Utility class for column name conversion
 */
public class ColumnNameUtils {
    /**
     * Converts a column index to a column name (e.g., 0 -> A, 1 -> B, 26 -> AA).
     *
     * @param index The column index
     * @return The column name
     */
    public static String getColumnName(int index) {
        StringBuilder columnName = new StringBuilder();
        while (index >= 0) {
            columnName.insert(0, (char) ('A' + index % 26));
            index = index / 26 - 1;
        }
        return columnName.toString();
    }
}
