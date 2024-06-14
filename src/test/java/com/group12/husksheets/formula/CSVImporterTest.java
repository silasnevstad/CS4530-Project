package com.group12.husksheets.formula;

//Owner:Jason King

import com.group12.husksheets.ui.utils.CSVImporter;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CSVImporterTest {

    private TableView<ObservableList<SimpleStringProperty>> tableView;

    @BeforeEach
    void setUp() {
        tableView = new TableView<>();
    }

    @Test
    void testImportCSV(@TempDir Path tempDir) throws IOException {
        File csvFile = tempDir.resolve("test.csv").toFile();
        try (FileWriter writer = new FileWriter(csvFile)) {
            writer.write("Name,Age,City\n");
            writer.write("Alice,30,New York\n");
            writer.write("Bob,25,Los Angeles\n");
            writer.write("Charlie,35,Chicago\n");
        }


        CSVImporter.importCSV(csvFile, tableView);

        assertEquals(3, tableView.getItems().size());
        assertEquals(4, tableView.getColumns().size()); // Includes row number column

        // Check headers
        assertEquals("", tableView.getColumns().get(0).getText());
        assertEquals("Name", tableView.getColumns().get(1).getText());
        assertEquals("Age", tableView.getColumns().get(2).getText());
        assertEquals("City", tableView.getColumns().get(3).getText());

        // Check row data
        ObservableList<SimpleStringProperty> row1 = tableView.getItems().get(0);
        assertEquals("Alice", row1.get(0).get());
        assertEquals("30", row1.get(1).get());
        assertEquals("New York", row1.get(2).get());

        ObservableList<SimpleStringProperty> row2 = tableView.getItems().get(1);
        assertEquals("Bob", row2.get(0).get());
        assertEquals("25", row2.get(1).get());
        assertEquals("Los Angeles", row2.get(2).get());

        ObservableList<SimpleStringProperty> row3 = tableView.getItems().get(2);
        assertEquals("Charlie", row3.get(0).get());
        assertEquals("35", row3.get(1).get());
        assertEquals("Chicago", row3.get(2).get());
    }

    /*
    @Test
    void testParseCSVLine() {
        String line = "Alice,30,\"New York, USA\"";
        String[] values = CSVImporter.parseCSVLine(line);
        assertArrayEquals(new String[]{"Alice", "30", "New York, USA"}, values);
    }

    @Test
    void testParseCSVLineWithCommasInQuotes() {
        String line = "\"Doe, John\",42,\"San Francisco, CA\"";
        String[] values = CSVImporter.parseCSVLine(line);
        assertArrayEquals(new String[]{"Doe, John", "42", "San Francisco, CA"}, values);
    }

    @Test
    void testParseEmptyCSVLine() {
        String line = "";
        String[] values = CSVImporter.parseCSVLine(line);
        assertArrayEquals(new String[]{""}, values);
    }

    @Test
    void testParseNullCSVLine() {
        String[] values = CSVImporter.parseCSVLine(null);
        assertArrayEquals(new String[0], values);
    }
    */

}

