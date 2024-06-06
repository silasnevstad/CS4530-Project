// Owner: Silas Nevstad

package com.group12.husksheets.server;

import static spark.Spark.*;

import com.group12.husksheets.server.controllers.HusksheetsController;
import com.group12.husksheets.server.services.DatabaseService;
import com.group12.husksheets.server.services.PublisherService;
import com.group12.husksheets.server.services.UserService;

public class HusksheetsServer {

    public static void main(String[] args) {
        // Get the database URL from command-line arguments or use the default SQLite database
        String databaseUrl = args.length > 0 ? args[0] : "jdbc:sqlite:husksheets.db";
        startServer(databaseUrl);
    }

    /**
     * Starts the Spark web server with the given database URL.
     * Sets up HTTPS, initializes the database, and sets up the controller.
     *
     * @param databaseUrl The URL of the database to connect to.
     */
    public static void startServer(String databaseUrl) {
        port(9443);

        // Configure HTTPS with a keystore
        secure("src/main/resources/keystore.jks", "husksheets", null, null);

        // Initialize the database with the provided URL
        DatabaseService.setDatabaseUrl(databaseUrl);
        DatabaseService.initializeDatabase();

        // Create service instances
        PublisherService publisherService = new PublisherService();
        UserService userService = new UserService();

        // Set up the controller with the services
        new HusksheetsController(publisherService, userService);
    }
}