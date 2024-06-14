package com.group12.husksheets.ui.controllers;

import com.group12.husksheets.ui.Main;
import com.group12.husksheets.ui.models.WelcomePage;
import com.group12.husksheets.server.services.UserService;

import com.group12.husksheets.ui.services.BackendService;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.Base64;

public class WelcomePageController {

  private final Stage stage;
  private final WelcomePage welcomePage;
  private final UserService userService;
  private BackendService backendService;
  private Main mainApp;

  @FXML
  private Button loginButton;

  @FXML
  private TextField publisherNameEntered;

  @FXML
  private TextField usernameEntered;

  @FXML
  private TextField passwordEntered;

  public WelcomePageController(Stage stage, Main mainApp) {
    this.stage = stage;
    this.userService = new UserService();
    this.welcomePage = new WelcomePage(this);
    this.mainApp = mainApp;
  }

  public void run() {
    try {
      Scene scene = welcomePage.load();
      stage.setScene(scene);
      stage.show();
      initializeButtonActions();
    } catch (IllegalStateException e) {
      e.printStackTrace();
    }
  }

  private void initializeButtonActions() {
    loginButton.setOnAction(e -> {
      try {
        tryUserLogin(publisherNameEntered.getText(), usernameEntered.getText(), passwordEntered.getText());
      } catch (Exception ex) {
        throw new RuntimeException(ex);
      }
    });
  }

  public void tryUserLogin(String publisherName, String username, String password) throws Exception {
    String authHeader = "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
    if (userService.isValidUser(authHeader)) {
      this.backendService = new BackendService(username, password);
      if (backendService.doesPublisherExist(publisherName)) {
        acceptUser(publisherName);
      } else {
        backendService.register(publisherName);
        acceptUser(publisherName);
      }
    } else {
      rejectUser();
    }
  }

  public void acceptUser(String publisherName) {
    createSheetSelectPage(publisherName);
  }

  public void rejectUser() {
    usernameEntered.clear();
    passwordEntered.clear();
  }

  // Owner: Zach Pulichino
  private void createSheetSelectPage(String publisherName) {
    try {
      SheetSelectPageController controller = new SheetSelectPageController();
      controller.setPublisherName(publisherName);
      controller.setStage(stage);
      controller.setBackendService(backendService);
      controller.setMainApp(mainApp);
      controller.run();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
