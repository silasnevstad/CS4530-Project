package com.group12.husksheets.server.controllers;

import com.group12.husksheets.ui.WelcomePage;
import com.group12.husksheets.server.services.UserService;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.Base64;

public class WelcomePageController {

  private final Stage stage;
  private WelcomePage welcomePage;
  private UserService userService;

  @FXML
  private Button loginExistingUserButton;

  @FXML
  private Button loginNewUserButton;

  @FXML
  private TextField usernameEntered;

  @FXML
  private TextField passwordEntered;

  public WelcomePageController(Stage stage) {
    this.stage = stage;
    this.userService = new UserService();
    this.welcomePage = new WelcomePage(this);
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
    loginExistingUserButton.setOnAction(e -> tryExistingUserLogin(usernameEntered.getText(), passwordEntered.getText()));
    loginNewUserButton.setOnAction(e -> tryNewUserLogin(usernameEntered.getText(), passwordEntered.getText()));
  }

  public void tryNewUserLogin(String username, String password) {
    String authHeader = "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
    if (userService.isValidUser(authHeader)) {
      acceptNewUser(username);
    } else {
      rejectNewUser();
    }
  }

  public void acceptNewUser(String username) {
    createSheetSelectPage(username);
  }

  public void rejectNewUser() {
    usernameEntered.clear();
    passwordEntered.clear();
  }

  public void tryExistingUserLogin(String username, String password) {
    String authHeader = "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
    if (userService.isValidUser(authHeader)) {
      acceptExistingUser(username);
    } else {
      rejectExistingUser();
    }
  }

  public void acceptExistingUser(String username) {
    createSheetSelectPage(username);
  }

  public void rejectExistingUser() {
    usernameEntered.clear();
    passwordEntered.clear();
  }

  private void createSheetSelectPage(String username) {
    try {
      SheetSelectPageController controller = new SheetSelectPageController();
      controller.setUsername(username);
      controller.setStage(new Stage());
      controller.run();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
