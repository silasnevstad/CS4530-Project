package com.group12.husksheets.server;

import static spark.Spark.*;

import com.group12.husksheets.server.controllers.HusksheetsController;
import com.group12.husksheets.server.services.DatabaseService;
import com.group12.husksheets.server.services.PublisherService;
import com.group12.husksheets.server.services.UserService;

public class HusksheetsServer {

    public static void main(String[] args) {
        String databaseUrl = args.length > 0 ? args[0] : "jdbc:sqlite:husksheets.db";
        startServer(databaseUrl);
    }

    public static void startServer(String databaseUrl) {
        port(9443);

        secure("src/main/resources/keystore.jks", "husksheets", null, null);

        DatabaseService.setDatabaseUrl(databaseUrl);
        DatabaseService.initializeDatabase();

        PublisherService publisherService = new PublisherService();
        UserService userService = new UserService();
        new HusksheetsController(publisherService, userService);
    }
}