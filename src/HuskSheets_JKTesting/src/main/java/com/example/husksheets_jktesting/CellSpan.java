package com.example.husksheets_jktesting;

/**
 * Class to represent cell span information
 */
class CellSpan {
    private final int startRow; // Starting row of the span
    private final int startCol; // Starting column of the span
    private final int endRow;   // Ending row of the span
    private final int endCol;   // Ending column of the span

    /**
     * Constructs a CellSpan object
     *
     * @param startRow The starting row
     * @param startCol The starting column
     * @param endRow   The ending row
     * @param endCol   The ending column
     */
    public CellSpan(int startRow, int startCol, int endRow, int endCol) {
        this.startRow = startRow;
        this.startCol = startCol;
        this.endRow = endRow;
        this.endCol = endCol;
    }

    /**
     * Checks if a cell is within the span
     *
     * @param row The row index
     * @param col The column index
     * @return True if the cell is within the span, false otherwise
     */
    public boolean isWithinSpan(int row, int col) {
        return row >= startRow && row <= endRow && col >= startCol && col <= endCol;
    }

    /**
     * Checks if a cell is the top-left cell of the span
     *
     * @param row The row index
     * @param col The column index
     * @return True if the cell is the top-left cell, false otherwise
     */
    public boolean isTopLeftCell(int row, int col) {
        return row == startRow && col == startCol;
    }
}
