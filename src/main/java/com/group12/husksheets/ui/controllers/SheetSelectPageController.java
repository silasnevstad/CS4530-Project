// Owner: Silas Nevstad, Nicholas Gillespie, Zach Pulichino
package com.group12.husksheets.ui.controllers;

import com.group12.husksheets.models.Argument;
import com.group12.husksheets.models.Result;
import com.group12.husksheets.ui.services.BackendService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SheetSelectPageController {

  private Stage stage;
  private String publisherName;

  @FXML
  private Label publisherNameLabel;

  @FXML
  private Button createNewSheetButton;

  @FXML
  private Button deleteSheetButton;

  @FXML
  private ListView<String> sheetsListView;

  private BackendService backendService;
  private SheetView sheetView;

  public SheetSelectPageController() {
    // Default constructor
  }

  public void setStage(Stage stage) {
    this.stage = stage;
  }

  public void setPublisherName(String publisherName) {
    this.publisherName = publisherName;
  }

  public void setBackendService(BackendService backendService) {
    this.backendService = backendService;
  }

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

  // Owner: Silas Nevstad and Zach Pulichino
  private void fetchSheets() {
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

  // Owner: Zach Pulichino and Silas Nevstad
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

  private void openSheet(String publisher, String sheet, boolean isOwned) {
    sheetView = new SheetView();
    sheetView.setBackendService(backendService);
    sheetView.showSpreadsheetView(stage, publisher, sheet, isOwned);
  }

  // Owner: Zach Pulichino
  private void showError(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }
}