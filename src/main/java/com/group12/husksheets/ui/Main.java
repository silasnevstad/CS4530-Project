// Owner: Zach Pulichino
package com.group12.husksheets.ui;


import com.group12.husksheets.ui.controllers.WelcomePageController;
import javafx.application.Application;
import javafx.stage.Stage;


public class Main extends Application {

  @Override
  public void start(Stage primaryStage) {
    showWelcomeScreen(primaryStage);
  }

  private void showWelcomeScreen(Stage primaryStage) {
    try {
      WelcomePageController controller = new WelcomePageController(primaryStage);
      controller.run();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    launch(args);
  }
}
