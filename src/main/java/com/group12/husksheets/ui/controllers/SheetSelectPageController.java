package com.group12.husksheets.ui.controllers;

import com.group12.husksheets.models.Argument;
import com.group12.husksheets.models.Result;
import com.group12.husksheets.ui.services.BackendService;
import com.group12.husksheets.ui.Main;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
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
  private ListView<String> sheetsListView;

  private Main mainApp; // Reference to the Main application

  private BackendService backendService;

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

  public void setMainApp(Main mainApp) {
      this.mainApp = mainApp;
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
      publisherNameLabel.setText(publisherName);

      fetchSheets();
    } catch (IOException e) {
      e.printStackTrace();
      throw new IllegalStateException("Unable to load SheetSelectPage.fxml", e);
    }
  }

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
      System.out.println("Unable to establish connection: " + e.getMessage());
    }
  }

  public void newSheet() {
    try {
      Result result = backendService.createSheet(publisherName, "NewSheet");
      if (result.success) {
        openSheet(publisherName, "NewSheet", true);
      } else {
        rejectNewSheet();
      }
    } catch (Exception e) {
      System.out.println("Failed to create new sheet: " + e.getMessage());
    }
  }

  public void rejectNewSheet() {
    System.out.println("Failed to create new sheet.");
  }

  private void openSheet(String publisher, String sheet, boolean isOwned) {
    mainApp.showSpreadsheetView(stage, publisher, sheet, isOwned);
  }
}
