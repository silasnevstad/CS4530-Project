package com.group12.husksheets.server.controllers;

import com.google.gson.Gson;
import com.group12.husksheets.models.Argument;
import com.group12.husksheets.ui.Main;
import com.group12.husksheets.models.Result;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import javax.net.ssl.HttpsURLConnection;

public class SheetSelectPageController {

  private Stage stage;
  private String username;

  @FXML
  private Label usernameLabel;

  @FXML
  private Button createNewSheetButton;

  @FXML
  private ListView<String> sheetsListView;

  private Main mainApp; // Reference to the Main application

  public SheetSelectPageController() {
    // Default constructor
  }

  public void setStage(Stage stage) {
    this.stage = stage;
  }

  public void setUsername(String username) {
    this.username = username;
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
      usernameLabel.setText(username);

      fetchSheets();
    } catch (IOException e) {
      e.printStackTrace();
      throw new IllegalStateException("Unable to load SheetSelectPage.fxml", e);
    }
  }

  private void fetchSheets() {
    Argument getSheetsArg = new Argument();
    getSheetsArg.publisher = username;
    Gson gson = new Gson();
    String getSheetsJsonArg = gson.toJson(getSheetsArg);

    ArrayList<String> accessibleSheets = new ArrayList<>();
    try {
      HttpURLConnection connection = createConnection("/api/v1/getSheets", "POST", getSheetsJsonArg);

      Result result = gson.fromJson(getResponse(connection), Result.class);

      for (Argument arg : result.value) {
        accessibleSheets.add(arg.sheet);
      }

      sheetsListView.getItems().addAll(accessibleSheets);

    } catch (IOException e) {
      System.out.println("Unable to establish connection");
    }
  }

  private HttpURLConnection createConnection(String endpoint, String method, String jsonBody) throws IOException {
    URL url = new URL("https://localhost:9443" + endpoint);
    HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
    connection.setRequestMethod(method);
    connection.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString((username + ":" + "password").getBytes(StandardCharsets.UTF_8))); // Replace with actual password management
    connection.setRequestProperty("Content-Type", "application/json");
    connection.setDoOutput(true);

    if (jsonBody != null) {
      try (OutputStream os = connection.getOutputStream()) {
        byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
        os.write(input, 0, input.length);
      }
    }
    return connection;
  }

  private String getResponse(HttpURLConnection connection) throws IOException {
    try (InputStream is = connection.getInputStream()) {
      return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }
  }

  public void newSheet() {
    String sheet = "";

    Argument createSheetArg = new Argument();
    createSheetArg.publisher = username;
    createSheetArg.sheet = sheet;
    Gson gson = new Gson();
    String createSheetJsonArg = gson.toJson(createSheetArg);

    try {
      HttpURLConnection connection = createConnection("/api/v1/createSheet", "POST", createSheetJsonArg);

      Result result = gson.fromJson(getResponse(connection), Result.class);

      if (result.success) {
        mainApp.openBlankSheet(stage); // Call the method to open a blank sheet in Main
      } else {
        rejectNewSheet();
      }
    } catch (IOException e) {
      System.out.println(e);
    }
  }

  public void rejectNewSheet() {
    System.out.println("Failed to create new sheet.");
  }
}
