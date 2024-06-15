package com.group12.husksheets.ui;

import com.group12.husksheets.models.Argument;
import com.group12.husksheets.models.Result;
import com.group12.husksheets.ui.controllers.SheetSelectPageController;
import com.group12.husksheets.ui.services.BackendService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SheetSelectPageControllerMock extends SheetSelectPageController {

    public Stage stage;
    public String publisherName;

    @FXML
    public Label publisherNameLabel;

    @FXML
    public Button createNewSheetButton;

    @FXML
    public Button deleteSheetButton;

    @FXML
    public ListView<String> sheetsListView;

    public Main mainApp; // Reference to the Main application

    public BackendService backendService;

    // Owner: Nicholas Gillespie
    public SheetSelectPageControllerMock() { super(); }

    // Owner: Nicholas Gillespie
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    // Owner: Nicholas Gillespie
    public void setPublisherName(String publisherName) {
        this.publisherName = publisherName;
    }

    // Owner: Nicholas Gillespie
    public void setBackendService(BackendService backendService) {
        this.backendService = backendService;
    }

    // Owner: Nicholas Gillespie
    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    // Owner: Nicholas Gillespie
    public void run() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SheetSelectPage.fxml"));
            loader.setController(this);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

            createNewSheetButton.setOnAction(e -> newSheet());
            deleteSheetButton.setOnAction(e -> deleteSelectedSheet());
            publisherNameLabel.setText(publisherName);

            fetchSheets();
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("Unable to load SheetSelectPage.fxml", e);
        }
    }

    // Owner: Nicholas Gillespie
    public void fetchSheets() {
        try {
            Result result = backendService.getSheets(publisherName);
            List<String> accessibleSheets = new ArrayList<>();
            for (Argument arg : result.value) {
                accessibleSheets.add(arg.sheet + " (owned by " + arg.publisher + ")");
            }
            sheetsListView.getItems().addAll(accessibleSheets);

            sheetsListView.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    String selectedSheet = sheetsListView.getSelectionModel().getSelectedItem();
                    String sheetName = selectedSheet.split(" \\(owned by ")[0];
                    String sheetPublisher = selectedSheet.split(" \\(owned by ")[1].replace(")", "");
                    boolean isOwned = sheetPublisher.equals(publisherName);
                    openSheet(sheetPublisher, sheetName, isOwned);
                }
            });

        } catch (Exception e) {
            showError("Unable to fetch sheets", e.getMessage());
        }
    }

    // Owner: Zach Pulichino
    public void newSheet() {
        TextInputDialog dialog = new TextInputDialog("NewSheet");
        dialog.setTitle("Create New Sheet");
        dialog.setHeaderText("Enter the name for the new sheet:");
        dialog.setContentText("Sheet name:");

        // Traditional way to get the response value.
        dialog.showAndWait().ifPresent(sheetName -> {
            try {
                Result result = backendService.createSheet(publisherName, sheetName);
                if (result.success) {
                    openSheet(publisherName, sheetName, true);
                } else {
                    showError("Failed to create new sheet", "The sheet could not be created.");
                }
            } catch (Exception e) {
                showError("Failed to create new sheet", e.getMessage());
            }
        });
    }

    // Owner: Zach Pulichino
    private void deleteSelectedSheet() {
        String selectedSheet = sheetsListView.getSelectionModel().getSelectedItem();
        if (selectedSheet != null) {
            String sheetName = selectedSheet.split(" \\(owned by ")[0];
            String sheetPublisher = selectedSheet.split(" \\(owned by ")[1].replace(")", "");
            if (sheetPublisher.equals(publisherName)) {
                try {
                    Result result = backendService.deleteSheet(publisherName, sheetName);
                    if (result.success) {
                        sheetsListView.getItems().remove(selectedSheet);
                    } else {
                        showError("Failed to delete sheet", "The sheet could not be deleted.");
                    }
                } catch (Exception e) {
                    showError("Failed to delete sheet", e.getMessage());
                }
            } else {
                showError("Cannot delete sheet", "You can only delete sheets you own.");
            }
        }
    }

    // Owner: Zach Pulichino
    public void openSheet(String publisher, String sheet, boolean isOwned) {
        mainApp.showSpreadsheetView(stage, publisher, sheet, isOwned);
    }

    // Owner: Zach Pulichino
    public void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Owner: Nicholas Gillespie
    public String getPublisherName() {
        return this.publisherName;
    }
}


public class SheetSelectPageControllerTest {

    private Stage stage;

    private Main mainApp;

    private SheetSelectPageControllerMock sheetSelectPageController;

    // Owner: Nicholas Gillespie
    @BeforeEach
    public void setUp() {
        sheetSelectPageController = new SheetSelectPageControllerMock();
        sheetSelectPageController.run();
    }

    // Owner: Nicholas Gillespie
    // Test setting the stage
    @Test
    public void testSetStage() {
        Stage stage = new Stage();
        sheetSelectPageController.setStage(stage);
        assertEquals(stage, sheetSelectPageController.getStage());
    }

    // Owner: Nicholas Gillespie
    // Test getting the stage
    @Test
    public void testGetStage() {
        Stage stage = new Stage();
        sheetSelectPageController.setStage(stage);
        assertEquals(stage, sheetSelectPageController.getStage());
    }

    // Owner: Nicholas Gillespie
    // Test setting the publisher name
    @Test
    public void testSetPublisherName() {
        String publisher = "publisherName1";
        sheetSelectPageController.setPublisherName(publisher);
        assertEquals(publisher, sheetSelectPageController.getPublisherName());
    }

    // Owner: Nicholas Gillespie
    // Test getting the publisher name
    @Test
    public void testGetPublisherName() {
        String publisher = "publisherName1";
        sheetSelectPageController.setPublisherName(publisher);
        assertEquals(publisher, sheetSelectPageController.getPublisherName());
    }

    // Owner: Nicholas Gillespie
    // Test getting the publisher name
    @Test
    public void testSetBackendService() {
        String username = "user1";
        String password = "password1";
        BackendService backendService = new BackendService(username, password);
        sheetSelectPageController.setBackendService(backendService);
        assertEquals(backendService, sheetSelectPageController.getBackendService());
    }

    // Owner: Nicholas Gillespie
    // Test getting the publisher name
    @Test
    public void testSetMainApp() {
        Main mainApp = new Main();
        sheetSelectPageController.setMainApp(mainApp);
        assertEquals(mainApp, sheetSelectPageController.getMainApp());
    }

    // Owner: Nicholas Gillespie
    // Test getting the publisher name
    @Test
    public void testFetchSheets() {
        sheetSelectPageController.setPublisherName("publisherName1");
        sheetSelectPageController.setBackendService(new BackendService("user1", "password1"));
        sheetSelectPageController.fetchSheets();
        assertEquals(false, sheetSelectPageController.sheetsListView.getItems().isEmpty());
    }

    // Owner: Nicholas Gillespie
    // Test getting the publisher name
    @Test
    public void testNewSheet() {
        sheetSelectPageController.setPublisherName("publisherName1");
        sheetSelectPageController.setBackendService(new BackendService("user1", "password1"));
        sheetSelectPageController.newSheet();
        assertEquals(false, sheetSelectPageController.sheetsListView.getItems().isEmpty());
    }

    // Owner: Nicholas Gillespie
    // Test opening sheet
    @Test
    public void testOpenSheet() {
        sheetSelectPageController.setPublisherName("publisherName1");
        String sheet = "newSheetCreated";
        assertDoesNotThrow(() -> sheetSelectPageController.openSheet("publisherName1", sheet, true));
    }

    // Owner: Nicholas Gillespie
    // Test showing an error
    @Test
    public void testShowError() {
        String title = "Title";
        String message = "Message";
        assertDoesNotThrow(() -> sheetSelectPageController.showError(title, message));
    }
}
