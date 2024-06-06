// Owner: Silas Nevstad

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
    public void testAddPublisherEmptyString() {
        assertFalse(publisherService.addPublisher(""));
    }

    @Test
    public void testAddPublisherNull() {
        assertFalse(publisherService.addPublisher(null));
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
    public void testCreateSheetSuccess() {
        publisherService.addPublisher("publisher");
        assertTrue(publisherService.createSheet("publisher", "newSheet"));
    }

    @Test
    public void testCreateSheetNonExistentPublisher() {
        assertFalse(publisherService.createSheet("nonExistentPublisher", "sheet"));
    }

    @Test
    public void testCreateSheetAlreadyExists() {
        publisherService.addPublisher("publisher");
        publisherService.createSheet("publisher", "sheet");
        assertFalse(publisherService.createSheet("publisher", "sheet"));
    }

    @Test
    public void testCreateSheetEmptyString() {
        publisherService.addPublisher("publisher");
        assertFalse(publisherService.createSheet("publisher", ""));
    }

    @Test
    public void testCreateSheetNull() {
        publisherService.addPublisher("publisher");
        assertFalse(publisherService.createSheet("publisher", null));
    }

    @Test
    public void testDeleteSheetSuccess() {
        publisherService.addPublisher("publisher");
        publisherService.createSheet("publisher", "sheetToDelete");
        assertTrue(publisherService.deleteSheet("publisher", "sheetToDelete"));
    }

    @Test
    public void testDeleteSheetNonExistentSheet() {
        publisherService.addPublisher("publisher");
        assertFalse(publisherService.deleteSheet("publisher", "nonExistentSheet"));
    }

    @Test
    public void testDeleteSheetNonExistentPublisher() {
        publisherService.addPublisher("publisher");
        publisherService.createSheet("publisher", "sheetToDelete");
        assertFalse(publisherService.deleteSheet("nonExistentPublisher", "sheetToDelete"));
    }

    @Test
    public void testDeleteSheetEmptyString() {
        publisherService.addPublisher("publisher");
        assertFalse(publisherService.deleteSheet("publisher", ""));
    }

    @Test
    public void testDeleteSheetNull() {
        publisherService.addPublisher("publisher");
        assertFalse(publisherService.deleteSheet("publisher", null));
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
    public void testGetSheetsForNonExistentPublisher() {
        List<String> sheets = publisherService.getSheets("nonExistentPublisher");
        assertEquals(0, sheets.size());
    }

    @Test
    public void testGetSheetsForEmptyStringPublisher() {
        List<String> sheets = publisherService.getSheets("");
        assertEquals(0, sheets.size());
    }

    @Test
    public void testGetSheetsForNullPublisher() {
        List<String> sheets = publisherService.getSheets(null);
        assertEquals(0, sheets.size());
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
    public void testUpdateEmptyInputs() {
        publisherService.addPublisher("publisher");
        publisherService.createSheet("publisher", "sheet");

        assertFalse(publisherService.updatePublished("publisher", "sheet", ""));
        assertFalse(publisherService.updatePublished("publisher", "sheet", null));
        assertFalse(publisherService.updatePublished("publisher", "", "payload"));
        assertFalse(publisherService.updatePublished("publisher", null, "payload"));
        assertFalse(publisherService.updatePublished("", "sheet", "payload"));
        assertFalse(publisherService.updatePublished(null, "sheet", "payload"));
        assertFalse(publisherService.updateSubscription("publisher", "sheet", ""));
        assertFalse(publisherService.updateSubscription("publisher", "sheet", null));
        assertFalse(publisherService.updateSubscription("publisher", "", "payload"));
        assertFalse(publisherService.updateSubscription("publisher", null, "payload"));
        assertFalse(publisherService.updateSubscription("", "sheet", "payload"));
        assertFalse(publisherService.updateSubscription(null, "sheet", "payload"));
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
    public void testGetUpdatesInvalidInputs() {
        publisherService.addPublisher("publisher");
        publisherService.createSheet("publisher", "sheet");

        List<Argument> updates = new ArrayList<>();
        updates.add(new Argument("publisher", "sheet", "1", "payload1"));
        updates.add(new Argument("publisher", "sheet", "2", "payload2"));

        updates.forEach(update -> publisherService.updatePublished("publisher", "sheet", update.payload));
        updates.forEach(update -> publisherService.updateSubscription("publisher", "sheet", update.payload));

        List<Argument> retrievedUpdates = publisherService.getUpdatesForSubscription("", "sheet", "0");
        assertEquals(0, retrievedUpdates.size());

        retrievedUpdates = publisherService.getUpdatesForPublished("", "sheet", "0");
        assertEquals(0, retrievedUpdates.size());

        retrievedUpdates = publisherService.getUpdatesForSubscription(null, "sheet", "0");
        assertEquals(0, retrievedUpdates.size());

        retrievedUpdates = publisherService.getUpdatesForPublished(null, "sheet", "0");
        assertEquals(0, retrievedUpdates.size());

        retrievedUpdates = publisherService.getUpdatesForSubscription("publisher", "", "0");
        assertEquals(0, retrievedUpdates.size());

        retrievedUpdates = publisherService.getUpdatesForPublished("publisher", "", "0");
        assertEquals(0, retrievedUpdates.size());

        retrievedUpdates = publisherService.getUpdatesForSubscription("publisher", null, "0");
        assertEquals(0, retrievedUpdates.size());

        retrievedUpdates = publisherService.getUpdatesForPublished("publisher", null, "0");
        assertEquals(0, retrievedUpdates.size());
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
