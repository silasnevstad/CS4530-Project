package com.group12.husksheets.controllers;
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

public class WelcomePageController {

    private final Stage stage;

    private WelcomePage welcomePage;

    @FXML
    private Button loginExistingUserButton;

    @FXML
    private Button loginNewUserButton;

    @FXML
    private TextField usernameEntered;

    public WelcomePageController(Stage stage) {
        this.stage = stage;
        this.welcomePage = new WelcomePage(this);
    }

    public void run() {
        loginExistingUserButton.setOnAction(e -> tryExistingUserLogin(usernameEntered.toString()));
        loginNewUserButton.setOnAction(e -> tryNewUserLogin(usernameEntered.toString()));

        try {
            Scene scene = welcomePage.load();
            stage.setScene(scene);
        } catch (IllegalStateException e) {
            return; 
        }
    }

    public void tryNewUserLogin(String username) {

        Argument arg = new Argument();
        arg.publisher = username;
        Gson gson = new Gson();
        String jsonArg = gson.toJson(arg);

        try {
            HttpURLConnection connection = createConnection("/api/v1/register", "POST", jsonArg);

            Result result = gson.fromJson(getResponse(connection), Result.class);

            if(result.success) {
                acceptNewUser(username);
            }
            else {
                rejectNewUser();
            }
        } catch (IOException e) {
            System.out.println("Could not establish connection");
        }
    }

    public void acceptNewUser(String username) {
        createSheetSelectPage(username);
    }

    public void rejectNewUser() {
        usernameEntered.clear();
        return;
    }

    public void tryExistingUserLogin(String username) {

        Argument existingPublisherArg = new Argument();
        existingPublisherArg.publisher = username;
        Gson gson = new Gson();
        String registerJsonArg = gson.toJson(existingPublisherArg);

        try {
            HttpURLConnection connection = createConnection("/api/v1/getPublishers", "GET", registerJsonArg);

            Result result = gson.fromJson(getResponse(connection), Result.class);

            if(result.value.contains(existingPublisherArg)) {
                acceptExistingUser(username);
            }
            else {
                rejectExistingUser();
            }
        } catch (IOException e) {
            System.out.println("Unable to establish connection");
        }
    }

    public void acceptExistingUser(String username) {
        createSheetSelectPage(username);
    }

    public void rejectExistingUser() {
        usernameEntered.clear();
        return;
    }

    public void createSheetSelectPage(String username) {
        SheetSelectPageController sspc = new SheetSelectPageController(stage, username);
        sspc.run();
        return;
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
