package com.group12.husksheets.models;

import com.group12.husksheets.server.controllers.WelcomePageController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;

public class WelcomePage {

  private final FXMLLoader loader;

  public WelcomePage(WelcomePageController controller) {
    this.loader = new FXMLLoader();
    this.loader.setLocation(getClass().getClassLoader().getResource("WelcomePage.fxml"));
    this.loader.setController(controller);
  }

  public Scene load() throws IllegalStateException {
    try {
      VBox root = this.loader.load();
      return new Scene(root);
    } catch (java.io.IOException e) {
      throw new IllegalStateException(e);
    }
  }
}
