package com.group12.husksheets.server.services;

import com.group12.husksheets.models.Argument;
import com.group12.husksheets.models.Publisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class PublisherServiceTest {
    private PublisherService publisherService;

    @BeforeEach
    public void setUp() {
        DatabaseService.setDatabaseUrl("jdbc:sqlite:husksheetsTest.db");
        DatabaseService.initializeDatabase();
        publisherService = new PublisherService();
        clearDatabase();
    }

    private void clearDatabase() {
        try (Connection conn = DatabaseService.getConnection()) {
            String[] tables = {"updates", "sheets", "publishers"};
            for (String table : tables) {
                String sql = "DELETE FROM " + table;
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testAddPublisher() {
        assertTrue(publisherService.addPublisher("newPublisher"));
        // Adding the same publisher again should return false
        assertFalse(publisherService.addPublisher("newPublisher"));
    }

    @Test
    public void testGetPublishers() {
        publisherService.addPublisher("publisher1");
        publisherService.addPublisher("publisher2");

        HashMap<String, Publisher> publishers = publisherService.getPublishers();
        assertEquals(2, publishers.size());
        assertTrue(publishers.containsKey("publisher1"));
        assertTrue(publishers.containsKey("publisher2"));
    }

    @Test
    public void testCreateSheet() {
        publisherService.addPublisher("publisher");
        assertTrue(publisherService.createSheet("publisher", "newSheet"));
        // Creating the same sheet again should return false
        assertFalse(publisherService.createSheet("publisher", "newSheet"));
        // Creating a sheet for a nonexistent publisher should return false
        assertFalse(publisherService.createSheet("nonexistentPublisher", "newSheet"));
    }

    @Test
    public void testDeleteSheet() {
        publisherService.addPublisher("publisher");
        publisherService.createSheet("publisher", "sheetToDelete");
        assertTrue(publisherService.deleteSheet("publisher", "sheetToDelete"));
        // Deleting the same sheet (nonexistent) again should return false
        assertFalse(publisherService.deleteSheet("publisher", "sheetToDelete"));
        // Deleting a sheet for a nonexistent publisher should return false
        publisherService.createSheet("publisher", "sheetToDelete");
        assertFalse(publisherService.deleteSheet("nonexistentPublisher", "sheetToDelete"));
    }

    @Test
    public void testGetSheets() {
        publisherService.addPublisher("publisher");
        publisherService.createSheet("publisher", "sheet1");
        publisherService.createSheet("publisher", "sheet2");

        List<String> sheets = publisherService.getSheets("publisher");
        assertEquals(2, sheets.size());
        assertTrue(sheets.contains("sheet1"));
        assertTrue(sheets.contains("sheet2"));
    }

    @Test
    public void testUpdatePublished() {
        publisherService.addPublisher("publisher");
        publisherService.createSheet("publisher", "sheet");

        assertTrue(publisherService.updatePublished("publisher", "sheet", "payload1"));
        assertTrue(publisherService.updatePublished("publisher", "sheet", "payload2"));

        List<Argument> updates = publisherService.getUpdatesForPublished("publisher", "sheet", "0");
        assertEquals(2, updates.size());
        assertEquals("payload1", updates.get(0).payload);
        assertEquals("payload2", updates.get(1).payload);
        assertNotNull(updates.get(0).id);
        assertNotNull(updates.get(1).id);
    }

    @Test
    public void testUpdateSubscription() {
        publisherService.addPublisher("publisher");
        publisherService.createSheet("publisher", "sheet");

        assertTrue(publisherService.updateSubscription("publisher", "sheet", "payload1"));
        assertTrue(publisherService.updateSubscription("publisher", "sheet", "payload2"));

        List<Argument> updates = publisherService.getUpdatesForSubscription("publisher", "sheet", "0");
        assertEquals(2, updates.size());
        assertEquals("payload1", updates.get(0).payload);
        assertEquals("payload2", updates.get(1).payload);
        assertNotNull(updates.get(0).id);
        assertNotNull(updates.get(1).id);
    }

    @Test
    public void testGetUpdatesForSubscription() {
        publisherService.addPublisher("publisher");
        publisherService.createSheet("publisher", "sheet");

        List<Argument> updates = new ArrayList<>();
        updates.add(new Argument("publisher", "sheet", "1", "payload1"));
        updates.add(new Argument("publisher", "sheet", "2", "payload2"));

        updates.forEach(update -> publisherService.updateSubscription("publisher", "sheet", update.payload));

        List<Argument> retrievedUpdates = publisherService.getUpdatesForSubscription("publisher", "sheet", "0");
        assertEquals(2, retrievedUpdates.size());
        assertEquals("payload1", retrievedUpdates.get(0).payload);
        assertEquals("payload2", retrievedUpdates.get(1).payload);

        String lastId = retrievedUpdates.get(retrievedUpdates.size() - 1).id;
        publisherService.updateSubscription("publisher", "sheet", "payload3");
        publisherService.updateSubscription("publisher", "sheet", "payload4");

        retrievedUpdates = publisherService.getUpdatesForSubscription("publisher", "sheet", lastId);
        assertEquals(2, retrievedUpdates.size());
        assertEquals("payload3", retrievedUpdates.get(0).payload);
        assertEquals("payload4", retrievedUpdates.get(1).payload);
    }

    @Test
    public void testGetUpdatesForPublished() {
        publisherService.addPublisher("publisher");
        publisherService.createSheet("publisher", "sheet");

        List<Argument> updates = new ArrayList<>();
        updates.add(new Argument("publisher", "sheet", "1", "payload1"));
        updates.add(new Argument("publisher", "sheet", "2", "payload2"));

        updates.forEach(update -> publisherService.updatePublished("publisher", "sheet", update.payload));

        List<Argument> retrievedUpdates = publisherService.getUpdatesForPublished("publisher", "sheet", "0");
        assertEquals(2, retrievedUpdates.size());
        assertEquals("payload1", retrievedUpdates.get(0).payload);
        assertEquals("payload2", retrievedUpdates.get(1).payload);

        String lastId = retrievedUpdates.get(retrievedUpdates.size() - 1).id;
        publisherService.updatePublished("publisher", "sheet", "payload3");
        publisherService.updatePublished("publisher", "sheet", "payload4");

        retrievedUpdates = publisherService.getUpdatesForPublished("publisher", "sheet", lastId);
        assertEquals(2, retrievedUpdates.size());
        assertEquals("payload3", retrievedUpdates.get(0).payload);
        assertEquals("payload4", retrievedUpdates.get(1).payload);
    }

    @Test
    public void testDeleteNonExistentSheet() {
        publisherService.addPublisher("publisher");
        assertFalse(publisherService.deleteSheet("publisher", "nonExistentSheet"));
    }

    @Test
    public void testGetUpdatesForNonExistentPublisher() {
        List<Argument> updates = publisherService.getUpdatesForSubscription("nonExistentPublisher", "sheet", "0");
        assertEquals(0, updates.size());

        updates = publisherService.getUpdatesForPublished("nonExistentPublisher", "sheet", "0");
        assertEquals(0, updates.size());
    }

    @Test
    public void testGetUpdatesForNonExistentSheet() {
        publisherService.addPublisher("publisher");
        List<Argument> updates = publisherService.getUpdatesForSubscription("publisher", "nonExistentSheet", "0");
        assertEquals(0, updates.size());

        updates = publisherService.getUpdatesForPublished("publisher", "nonExistentSheet", "0");
        assertEquals(0, updates.size());
    }

    @Test
    public void testUpdateWithSpecialCharacters() {
        publisherService.addPublisher("publisher");
        publisherService.createSheet("publisher", "sheet");

        String specialPayload = "payload!@#$%^&*()_+{}|:\"<>?`~-=[]\\;',./";
        assertTrue(publisherService.updatePublished("publisher", "sheet", specialPayload));

        List<Argument> updates = publisherService.getUpdatesForPublished("publisher", "sheet", "0");
        assertEquals(1, updates.size());
        assertEquals(specialPayload, updates.get(0).payload);
    }

    @Test
    public void testUpdateWithLongString() {
        publisherService.addPublisher("publisher");
        publisherService.createSheet("publisher", "sheet");

        String longPayload = "a".repeat(10000); // 10,000 characters long
        assertTrue(publisherService.updatePublished("publisher", "sheet", longPayload));

        List<Argument> updates = publisherService.getUpdatesForPublished("publisher", "sheet", "0");
        assertEquals(1, updates.size());
        assertEquals(longPayload, updates.get(0).payload);
    }
}
