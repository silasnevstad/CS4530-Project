package org.example;

// Class to represent cell span information
class CellSpan {
    private final int startRow;
    private final int startCol;
    private final int endRow;
    private final int endCol;

    public CellSpan(int startRow, int startCol, int endRow, int endCol) {
        this.startRow = startRow;
        this.startCol = startCol;
        this.endRow = endRow;
        this.endCol = endCol;
    }

    public boolean isWithinSpan(int row, int col) {
        return row >= startRow && row <= endRow && col >= startCol && col <= endCol;
    }

    public boolean isTopLeftCell(int row, int col) {
        return row == startRow && col == startCol;
    }
}