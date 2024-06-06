package com.group12.husksheets.view;

import com.group12.husksheets.controllers.WelcomePageController;
import com.group12.husksheets.models.User;

import io.jsonwebtoken.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

import com.group12.husksheets.models.Sheet;
import java.util.ArrayList;

public class WelcomePage {

    javafx.fxml.FXMLLoader loader;

    WelcomePageController controller;

    public WelcomePage(WelcomePageController application) {
        this.controller = application;

        this.loader = new FXMLLoader();
        this.loader.setLocation(getClass().getClassLoader().getResource("WelcomePage.fxml"));

        this.loader.setController(application);
    }

    public Scene load() throws IllegalStateException {

        try {
            return this.loader.load();
        } catch (java.io.IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
