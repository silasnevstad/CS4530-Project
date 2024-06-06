// Owner: Silas Nevstad

package com.group12.husksheets.server.services;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseService {
    // Default SQLite database URL
    private static String databaseUrl = "jdbc:sqlite:husksheets.db";

    /**
     * Sets the database URL to be used by the DatabaseService.
     *
     * @param url The database URL.
     */
    public static void setDatabaseUrl(String url) {
        databaseUrl = url;
    }

    /**
     * Gets a connection to the database.
     *
     * @return A Connection object to the database.
     * @throws SQLException If a database access error occurs.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(databaseUrl);
    }

    /**
     * Initializes the database by creating the necessary tables if they do not exist.
     */
    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Create the publishers table
            String createPublishersTable = "CREATE TABLE IF NOT EXISTS publishers ("
                    + "name TEXT PRIMARY KEY)";
            stmt.execute(createPublishersTable);

            // Create the sheets table
            String createSheetsTable = "CREATE TABLE IF NOT EXISTS sheets ("
                    + "publisher TEXT,"
                    + "sheet TEXT,"
                    + "PRIMARY KEY (publisher, sheet))";
            stmt.execute(createSheetsTable);

            // Create the updates table
            String createUpdatesTable = "CREATE TABLE IF NOT EXISTS updates ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "publisher TEXT,"
                    + "sheet TEXT,"
                    + "payload TEXT,"
                    + "timestamp LONG,"
                    + "type TEXT)"; // 'published' for owners or 'subscription' for subscribers
            stmt.execute(createUpdatesTable);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
