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
    private static final int MAX_RETRIES = 3;

    public boolean addPublisher(String name) {
        if (publisherExists(name)) {
            return false;
        }
        String sql = "INSERT INTO publishers(name) VALUES(?)";
        return executeUpdateWithRetry(sql, pstmt -> pstmt.setString(1, name));
    }

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

    public boolean createSheet(String publisher, String sheet) {
        if (sheetExists(publisher, sheet) || !publisherExists(publisher)) {
            return false;
        }
        String sql = "INSERT INTO sheets(publisher, sheet) VALUES(?, ?)";
        return executeUpdateWithRetry(sql, pstmt -> {
            pstmt.setString(1, publisher);
            pstmt.setString(2, sheet);
        });
    }

    public boolean deleteSheet(String publisher, String sheet) {
        if (!sheetExists(publisher, sheet)) {
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

    public List<String> getSheets(String publisher) {
        List<String> sheets = new ArrayList<>();
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

    public List<Argument> getUpdatesForSubscription(String publisher, String sheet, String id) {
        return getAllUpdates(publisher, sheet, id);
    }

    public List<Argument> getUpdatesForPublished(String publisher, String sheet, String id) {
        return getAllUpdates(publisher, sheet, id);
    }

    private List<Argument> getAllUpdates(String publisher, String sheet, String id) {
        List<Argument> result = new ArrayList<>();
        String sql;

        // Check if the initial id is "0"
        if ("0".equals(id)) {
            sql = "SELECT * FROM updates WHERE publisher = ? AND sheet = ?";
            try (Connection conn = DatabaseService.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, publisher);
                pstmt.setString(2, sheet);
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
            sql = "SELECT * FROM updates WHERE publisher = ? AND sheet = ? AND id > ?";
            try (Connection conn = DatabaseService.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, publisher);
                pstmt.setString(2, sheet);
                pstmt.setString(3, id);
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

    public boolean updatePublished(String publisher, String sheet, String payload) {
        return addUpdate(publisher, sheet, payload, "published");
    }

    public boolean updateSubscription(String publisher, String sheet, String payload) {
        return addUpdate(publisher, sheet, payload, "subscription");
    }

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

    @FunctionalInterface
    public interface SQLConsumer<T> {
        void accept(T t) throws SQLException;
    }
}
