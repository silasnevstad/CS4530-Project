package com.group12.husksheets.controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

public class SheetSelectPage {

    javafx.fxml.FXMLLoader loader;

    SheetSelectPageController controller;

    public SheetSelectPage(SheetSelectPageController sscp) {
        this.controller = sscp;

        this.loader = new FXMLLoader();
        this.loader.setLocation(getClass().getClassLoader().getResource("SheetSelectPage.fxml"));

        this.loader.setController(sscp);
    }

    public Scene load() throws IllegalStateException {

        try {
            return this.loader.load();
        } catch (java.io.IOException e) {
            throw new IllegalStateException(e);
        }
    }

}
