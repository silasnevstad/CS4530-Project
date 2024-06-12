package com.group12.husksheets.ui.utils;

/**
 * Utility class for column name conversions
 */
public class ColumnNameUtils {

    /**
     * Converts a column index to a column name
     *
     * @param index The column index (0-based)
     * @return The column name (e.g., A, B, C, ..., Z, AA, AB, ...)
     */
    public static String getColumnName(int index) {
        StringBuilder sb = new StringBuilder();
        while (index >= 0) {
            sb.insert(0, (char) ('A' + (index % 26)));
            index = (index / 26) - 1;
        }
        return sb.toString();
    }

    /**
     * Converts a column name to a column index
     *
     * @param name The column name (e.g., A, B, C, ..., Z, AA, AB, ...)
     * @return The column index (0-based)
     */
    public static int getColumnIndex(String name) {
        int index = 0;
        for (int i = 0; i < name.length(); i++) {
            index = index * 26 + (name.charAt(i) - 'A' + 1);
        }
        return index - 1;
    }
}
