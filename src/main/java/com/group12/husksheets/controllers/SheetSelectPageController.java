package com.group12.husksheets.controllers;

import com.group12.husksheets.models.User;
import com.group12.husksheets.models.Argument;
import com.group12.husksheets.models.Result;
import com.group12.husksheets.models.Sheet;
import com.group12.husksheets.view.WelcomePage;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import com.google.gson.Gson;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;

import javax.net.ssl.HttpsURLConnection;

import com.google.gson.Gson;
import com.group12.husksheets.models.Argument;
import com.group12.husksheets.models.Result;
import com.group12.husksheets.models.Sheet;
import com.group12.husksheets.models.User;
import com.group12.husksheets.models.SpreadsheetPage;
import com.group12.husksheets.view.EditSheetButton;
import com.group12.husksheets.view.WelcomePage;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SheetSelectPageController {

    private String username;

    private final Stage stage;

    private SheetSelectPage sheetSelectPage;

    @FXML
    private Label usernameLabel;

    @FXML
    private Button createNewSheetButton;

    @FXML
    private VBox sheetsVBox;

    public SheetSelectPageController(Stage stage, String username) {
        this.stage = stage;
        this.username = username;
        this.sheetSelectPage = new SheetSelectPage(this);
    }

    public void run() {
        createNewSheetButton.setOnAction(e -> newSheet());

        Argument getSheetsArg = new Argument();
        getSheetsArg.publisher = "testPublisher5";
        Gson gson = new Gson();
        String getSheetsJsonArg = gson.toJson(getSheetsArg);

        ArrayList<String> accessibleSheets = new ArrayList<>();
        try {
            HttpURLConnection connection = createConnection("/api/v1/getSheets", "POST", getSheetsJsonArg);

            Result result = gson.fromJson(getResponse(connection), Result.class);

            for(Argument arg : result.value) {
                accessibleSheets.add(arg.sheet);
            }
        } catch (IOException e) {
            System.out.println("Unable to establish connection");
        }

        for(String sheet : accessibleSheets) {
            Button editSheetButton = new EditSheetButton(sheetsVBox, sheet)
            sheetsVBox.getChildren().addAll(editSheetButton);
            editSheetButton.setOnAction(e -> openSheet(sheet));
        }

        try {
            Scene scene = sheetSelectPage.load();
            stage.setScene(scene);
        } catch (IllegalStateException e) {
            return; 
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
    
            if(result.success) {
                openSheet(sheet);
            }
            else {
                rejectNewSheet();
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public void rejectNewSheet() {
        return;
    }

    public void openSheet(String sheet) {
        SpreadsheetPage ssp = new SpreadsheetPage();
        ssp.run(stage);
    }

    private HttpURLConnection createConnection(String endpoint, String method, String jsonBody) throws IOException {

        URL url = new URL("https://localhost:9443" + endpoint);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString("testPublisher:testPublisher".getBytes(StandardCharsets.UTF_8)));
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
}
