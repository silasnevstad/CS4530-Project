package com.group12.husksheets.ui.models;

import com.group12.husksheets.ui.controllers.SheetSelectPageController;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;

public class SheetSelectPage {
  private final SheetSelectPageController controller;

  public SheetSelectPage(SheetSelectPageController controller) {
    this.controller = controller;
  }

  public Parent load() {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/SheetSelectPage.fxml"));
      loader.setController(controller);
      return loader.load();
    } catch (IOException e) {
      e.printStackTrace();
      throw new IllegalStateException("Unable to load SheetSelectPage.fxml", e);
    }
  }
}
