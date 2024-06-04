package com.group12.husksheets.services;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseService {
    private static String databaseUrl = "jdbc:sqlite:husksheets.db";

    public static void setDatabaseUrl(String url) {
        databaseUrl = url;
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(databaseUrl);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Publishers table
            String createPublishersTable = "CREATE TABLE IF NOT EXISTS publishers ("
                    + "name TEXT PRIMARY KEY)";
            stmt.execute(createPublishersTable);

            // Sheets table
            String createSheetsTable = "CREATE TABLE IF NOT EXISTS sheets ("
                    + "publisher TEXT,"
                    + "sheet TEXT,"
                    + "PRIMARY KEY (publisher, sheet))";
            stmt.execute(createSheetsTable);

            // Updates table
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
