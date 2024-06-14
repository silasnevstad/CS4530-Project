// Owner: Silas Nevstad

package com.group12.husksheets.server.services;

import com.group12.husksheets.models.Argument;
import com.group12.husksheets.models.Publisher;
import org.sqlite.SQLiteErrorCode;

import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class PublisherService {
    // Maximum number of retries when database is busy
    private static final int MAX_RETRIES = 3;

    /**
     * Adds a new publisher to the database.
     *
     * @param name The name of the publisher.
     * @return true if the publisher was added, false if it already exists.
     */
    public boolean addPublisher(String name) {
        if (isInvalidInput(name) || publisherExists(name)) {
            return false;
        }
        String sql = "INSERT INTO publishers(name) VALUES(?)";
        return executeUpdateWithRetry(sql, pstmt -> pstmt.setString(1, name));
    }

    /**
     * Checks if a publisher exists in the database.
     *
     * @param name The name of the publisher.
     * @return true if the publisher exists, false otherwise.
     */
    private boolean publisherExists(String name) {
        String sql = "SELECT 1 FROM publishers WHERE name = ?";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves all publishers from the database.
     *
     * @return A HashMap containing publisher names as keys and Publisher objects as values.
     */
    public HashMap<String, Publisher> getPublishers() {
        HashMap<String, Publisher> publishers = new HashMap<>();
        String sql = "SELECT * FROM publishers";
        try (Connection conn = DatabaseService.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                publishers.put(rs.getString("name"), new Publisher(rs.getString("name")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return publishers;
    }

    /**
     * Creates a new sheet for a publisher.
     *
     * @param publisher The name of the publisher.
     * @param sheet The name of the sheet.
     * @return true if the sheet was created, false if it already exists or the publisher does not exist.
     */
    public boolean createSheet(String publisher, String sheet) {
        if (isInvalidInput(publisher, sheet) || sheetExists(publisher, sheet) || !publisherExists(publisher)) {
            return false;
        }
        String sql = "INSERT INTO sheets(publisher, sheet) VALUES(?, ?)";
        return executeUpdateWithRetry(sql, pstmt -> {
            pstmt.setString(1, publisher);
            pstmt.setString(2, sheet);
        });
    }

    /**
     * Deletes a sheet for a publisher.
     *
     * @param publisher The name of the publisher.
     * @param sheet The name of the sheet.
     * @return true if the sheet was deleted, false if it does not exist.
     */
    public boolean deleteSheet(String publisher, String sheet) {
        if (isInvalidInput(publisher, sheet) || !sheetExists(publisher, sheet)) {
            return false;
        }
        String deleteSheetSql = "DELETE FROM sheets WHERE publisher = ? AND sheet = ?";
        String deleteUpdatesSql = "DELETE FROM updates WHERE publisher = ? AND sheet = ?";

        return executeUpdateWithRetry(deleteSheetSql, pstmt -> {
            pstmt.setString(1, publisher);
            pstmt.setString(2, sheet);
        }) && executeUpdateWithRetry(deleteUpdatesSql, pstmt -> {
            pstmt.setString(1, publisher);
            pstmt.setString(2, sheet);
        });
    }

    /**
     * Checks if a sheet exists for a given publisher.
     *
     * @param publisher The name of the publisher.
     * @param sheet The name of the sheet.
     * @return true if the sheet exists, false otherwise.
     */
    private boolean sheetExists(String publisher, String sheet) {
        String sql = "SELECT 1 FROM sheets WHERE publisher = ? AND sheet = ?";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, publisher);
            pstmt.setString(2, sheet);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves all sheets for a given publisher.
     *
     * @param publisher The name of the publisher.
     * @return A list of sheet names.
     */
    public List<String> getSheets(String publisher) {
        List<String> sheets = new ArrayList<>();
        if (isInvalidInput(publisher)) {
            return sheets;
        }
        String sql = "SELECT sheet FROM sheets WHERE publisher = ?";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, publisher);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                sheets.add(rs.getString("sheet"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sheets;
    }

    /**
     * Retrieves updates for a subscription.
     *
     * @param publisher The name of the publisher.
     * @param sheet The name of the sheet.
     * @param id The ID of the last known update.
     * @return A list of Argument objects representing the updates.
     */
    public List<Argument> getUpdatesForSubscription(String publisher, String sheet, String id) {
        if (isInvalidInput(publisher, sheet)) {
            return new ArrayList<>();
        }
        return getUpdates(publisher, sheet, id, "published");
    }

    /**
     * Retrieves updates for a published sheet.
     *
     * @param publisher The name of the publisher.
     * @param sheet The name of the sheet.
     * @param id The ID of the last known update.
     * @return A list of Argument objects representing the updates.
     */
    public List<Argument> getUpdatesForPublished(String publisher, String sheet, String id) {
        if (isInvalidInput(publisher, sheet)) {
            return new ArrayList<>();
        }
        return getUpdates(publisher, sheet, id, "subscription");
    }

    /**
     * Retrieves all updates for a given publisher and sheet starting from a specific ID.
     *
     * @param publisher The name of the publisher.
     * @param sheet The name of the sheet.
     * @param id The ID of the last known update.
     * @return A list of Argument objects representing the updates.
     */
    private List<Argument> getUpdates(String publisher, String sheet, String id, String type) {
        List<Argument> result = new ArrayList<>();
        String sql;

        // Check if the initial id is "0"
        if ("0".equals(id)) {
            sql = "SELECT * FROM updates WHERE publisher = ? AND sheet = ? AND type = ?";
            try (Connection conn = DatabaseService.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, publisher);
                pstmt.setString(2, sheet);
                pstmt.setString(3, type);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    Argument argument = new Argument();
                    argument.id = rs.getString("id");
                    argument.publisher = rs.getString("publisher");
                    argument.sheet = rs.getString("sheet");
                    argument.payload = rs.getString("payload");
                    result.add(argument);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            // Fetch updates after the given id
            sql = "SELECT * FROM updates WHERE publisher = ? AND sheet = ? AND id > ? AND type = ?";
            try (Connection conn = DatabaseService.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, publisher);
                pstmt.setString(2, sheet);
                pstmt.setString(3, id);
                pstmt.setString(4, type);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    Argument argument = new Argument();
                    argument.id = rs.getString("id");
                    argument.publisher = rs.getString("publisher");
                    argument.sheet = rs.getString("sheet");
                    argument.payload = rs.getString("payload");
                    result.add(argument);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * Updates a published sheet with new content.
     *
     * @param publisher The name of the publisher.
     * @param sheet The name of the sheet.
     * @param payload The new content to be added.
     * @return true if the update was successful, false otherwise.
     */
    public boolean updatePublished(String publisher, String sheet, String payload) {
        if (isInvalidInput(publisher, sheet, payload)) {
            return false;
        }
        String[] updates = payload.split("\n");
        for (String update : updates) {
            if (!addUpdate(publisher, sheet, update, "published")) {
                return false;
            }
        }
        return true;
    }

    /**
     * Updates a subscription sheet with new content.
     *
     * @param publisher The name of the publisher.
     * @param sheet The name of the sheet.
     * @param payload The new content to be added.
     * @return true if the update was successful, false otherwise.
     */
    public boolean updateSubscription(String publisher, String sheet, String payload) {
        if (isInvalidInput(publisher, sheet, payload)) {
            return false;
        }
        String[] updates = payload.split("\n");
        for (String update : updates) {
            if (!addUpdate(publisher, sheet, update, "subscription")) {
                return false;
            }
        }
        return true;
    }

    /**
     * Adds a new update to the updates table.
     *
     * @param publisher The name of the publisher.
     * @param sheet The name of the sheet.
     * @param payload The content of the update.
     * @param type The type of the update (published or subscription).
     * @return true if the update was added, false otherwise.
     */
    private boolean addUpdate(String publisher, String sheet, String payload, String type) {
        String sql = "INSERT INTO updates (publisher, sheet, payload, timestamp, type) VALUES (?, ?, ?, ?, ?)";
        return executeUpdateWithRetry(sql, pstmt -> {
            long timestamp = System.currentTimeMillis();
            pstmt.setString(1, publisher);
            pstmt.setString(2, sheet);
            pstmt.setString(3, payload);
            pstmt.setLong(4, timestamp);
            pstmt.setString(5, type);
        });
    }

    /**
     * Executes an update SQL statement with retry logic if the database is busy.
     *
     * @param sql The SQL statement to execute.
     * @param consumer A consumer that sets the parameters of the PreparedStatement.
     * @return true if the update was successful, false otherwise.
     */
    private boolean executeUpdateWithRetry(String sql, SQLConsumer<PreparedStatement> consumer) {
        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            try (Connection conn = DatabaseService.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                consumer.accept(pstmt);
                pstmt.executeUpdate();
                return true;
            } catch (SQLException e) {
                if (e.getErrorCode() == SQLiteErrorCode.SQLITE_BUSY.code) { // Retry on busy database
                    try {
                        Thread.sleep(50); // Wait before retrying
                    } catch (InterruptedException interruptedException) {
                        Thread.currentThread().interrupt();
                    }
                } else { // Log and return false on other errors
                    e.printStackTrace();
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * Validates if the input strings are not null or empty.
     *
     * @param inputs The strings to validate.
     * @return true if all inputs are valid, false otherwise.
     */
    public boolean isInvalidInput(String... inputs) {
        for (String input : inputs) {
            if (input == null || input.trim().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @FunctionalInterface
    public interface SQLConsumer<T> {
        void accept(T t) throws SQLException;
    }
}
