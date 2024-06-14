package com.group12.husksheets.formula;

//Owner:Jason King and Silas Nevstad

import com.group12.husksheets.ui.utils.CSVImporter;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.testfx.framework.junit5.ApplicationTest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CSVImporterTest extends ApplicationTest {

    private TableView<ObservableList<SimpleStringProperty>> tableView;

    @Override
    public void start(Stage stage) {
        tableView = new TableView<>();
        Scene scene = new Scene(tableView);
        stage.setScene(scene);
        stage.show();
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

        Platform.runLater(() -> CSVImporter.importCSV(csvFile, tableView));

        waitForFxEvents();

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

    private void waitForFxEvents() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

