package com.group12.husksheets.ui;

import com.google.gson.Gson;
import com.group12.husksheets.server.controllers.HusksheetsController;
import com.group12.husksheets.server.services.PublisherService;
import com.group12.husksheets.server.services.UserService;
import com.group12.husksheets.ui.controllers.SheetSelectPageController;
import com.group12.husksheets.ui.controllers.WelcomePageController;
import com.group12.husksheets.ui.models.User;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

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

class WelcomePageControllerMock extends WelcomePageController {

    @FXML
    public Button loginButton;

    @FXML
    public TextField publisherNameEntered;

    @FXML
    public TextField usernameEntered;

    @FXML
    public TextField passwordEntered;

    public WelcomePageControllerMock(Stage stage, Main mainApp) {
        super(stage);
    }

    // Owner: Nicholas Gillespie
    public void run() {
        try {
            Scene scene = welcomePage.load();
            stage.setScene(scene);
            initializeButtonActions();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    // Owner: Nicholas Gillespie
    public void initializeButtonActions() {
        loginButton.setOnAction(e -> {
            try {
                tryUserLogin(publisherNameEntered.getText(), usernameEntered.getText(), passwordEntered.getText());
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    // Owner: Zach Pulichino
    public void tryUserLogin(String publisherName, String username, String password) throws Exception {
        String authHeader = "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
        if (userService.isValidAuth(authHeader)) {
            this.backendService = new BackendService(username, password);
            if (backendService.doesPublisherExist(publisherName)) {
                acceptUser(publisherName);
            } else {
                backendService.register();
                acceptUser(publisherName);
            }
        } else {
            rejectUser();
        }
    }

    // Owner: Nicholas Gillespie
    public void acceptUser(String publisherName) {
        createSheetSelectPage(publisherName);
    }

    // Owner: Nicholas Gillespie
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


public class WelcomePageControllerTest {

    private Stage stage;

    private Main mainApp;

    private WelcomePageControllerMock welcomePageController;

    // Owner: Nicholas Gillespie
    @BeforeEach
    public void setUp() {
        welcomePageController = new WelcomePageControllerMock(stage, mainApp);
        welcomePageController.run();
    }

    // Owner: Nicholas Gillespie
    // Test initializing button actions
    @Disabled
    @Test
    public void testInitializeButtonActions() {
        welcomePageController.initializeButtonActions();
        assertDoesNotThrow(() -> welcomePageController.initializeButtonActions());
    }

    // Owner: Nicholas Gillespie
    // Test rejecting a user login
    @Disabled
    @Test
    public void testRejectUser() {
        welcomePageController.usernameEntered.setText("hello");
        welcomePageController.passwordEntered.setText("world");
        welcomePageController.rejectUser();
        assertEquals("", welcomePageController.usernameEntered);
        assertEquals("", welcomePageController.passwordEntered);
    }

    // Owner: Nicholas Gillespie
    // Test rejecting a user login
    @Disabled
    @Test
    public void testTryUserLoginRejected() {
        String publisherName = "publisherName";
        String username = "hello";
        String password = "world";

        welcomePageController.usernameEntered.setText(username);
        welcomePageController.passwordEntered.setText(password);

        try {
            welcomePageController.tryUserLogin(publisherName, username, password);
            assertEquals("", welcomePageController.usernameEntered);
            assertEquals("", welcomePageController.passwordEntered);
        } catch (Exception e) {
            return;
        }
    }

    // Owner: Nicholas Gillespie
    // Test accepting a user login
    @Disabled
    @Test
    public void testTryUserLoginAccepted() {
        String publisherName = "publisher1";
        String username = "user1";
        String password = "password1";

        welcomePageController.usernameEntered.setText(username);
        welcomePageController.passwordEntered.setText(password);

        BackendService bs = new BackendService(username, password);


        try {
            welcomePageController.tryUserLogin(publisherName, username, password);
            assertEquals("", welcomePageController.usernameEntered);
            assertEquals("", welcomePageController.passwordEntered);
        } catch (Exception e) {
            return;
        }
    }

    // Owner: Nicholas Gillespie
    // Test accepting a user
    @Disabled
    @Test
    public void testAcceptUser() {
        String publisherName = "publisher1";
        String username = "user1";
        String password = "password1";

        welcomePageController.usernameEntered.setText(username);
        welcomePageController.passwordEntered.setText(password);

        SheetSelectPageController controller = new SheetSelectPageController();
        welcomePageController.acceptUser(publisherName, controller);
        assertEquals(publisherName, controller.getPublisherName());
    }

    // Owner: Nicholas Gillespie
    // Test creating sheet select page
    @Disabled
    @Test
    public void testCreateSheetSelectPage() {
        SheetSelectPageController sspc = new SheetSelectPageController();

        String publisherName = "publisher1";

        welcomePageController.createSheetSelectPage(publisherName, sspc);
        SheetSelectPageController controller = new SheetSelectPageController();
        assertEquals(publisherName, controller.getPublisherName());
    }
}
