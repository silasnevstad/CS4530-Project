package com.group12.husksheets.formula;

import com.group12.husksheets.ui.utils.ColumnNameUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


//Owner:Jason King



class ColumnNameUtilsTest {

    @Test
    void testGetColumnName() {
        assertEquals("A", ColumnNameUtils.getColumnName(0));
        assertEquals("B", ColumnNameUtils.getColumnName(1));
        assertEquals("Z", ColumnNameUtils.getColumnName(25));
        assertEquals("AA", ColumnNameUtils.getColumnName(26));
        assertEquals("AB", ColumnNameUtils.getColumnName(27));
        assertEquals("AZ", ColumnNameUtils.getColumnName(51));
        assertEquals("BA", ColumnNameUtils.getColumnName(52));
        assertEquals("ZZ", ColumnNameUtils.getColumnName(701));
        assertEquals("AAA", ColumnNameUtils.getColumnName(702));
    }

    @Test
    void testGetColumnIndex() {
        assertEquals(0, ColumnNameUtils.getColumnIndex("A"));
        assertEquals(1, ColumnNameUtils.getColumnIndex("B"));
        assertEquals(25, ColumnNameUtils.getColumnIndex("Z"));
        assertEquals(26, ColumnNameUtils.getColumnIndex("AA"));
        assertEquals(27, ColumnNameUtils.getColumnIndex("AB"));
        assertEquals(51, ColumnNameUtils.getColumnIndex("AZ"));
        assertEquals(52, ColumnNameUtils.getColumnIndex("BA"));
        assertEquals(701, ColumnNameUtils.getColumnIndex("ZZ"));
        assertEquals(702, ColumnNameUtils.getColumnIndex("AAA"));
    }

    @Test
    void testRoundTripConversion() {
        for (int i = 0; i < 1000; i++) {
            String columnName = ColumnNameUtils.getColumnName(i);
            int columnIndex = ColumnNameUtils.getColumnIndex(columnName);
            assertEquals(i, columnIndex, "Failed at index: " + i);
        }
    }
}

