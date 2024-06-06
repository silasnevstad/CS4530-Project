package com.group12.husksheets.server.controllers;

import com.group12.husksheets.models.Argument;
import com.group12.husksheets.models.Publisher;
import com.group12.husksheets.models.Result;
import com.group12.husksheets.server.services.PublisherService;
import com.group12.husksheets.server.services.UserService;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import spark.Request;
import spark.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class HusksheetsControllerTest {
    @Mock
    private PublisherService publisherService;

    @Mock
    private UserService userService;

    @InjectMocks
    private HusksheetsController husksheetsController;

    private final Gson gson = new Gson();

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        husksheetsController = new HusksheetsController(publisherService, userService);
    }

    @Test
    public void testRegisterPublisher() {
        Request req = mock(Request.class);
        Response res = mock(Response.class);

        when(req.body()).thenReturn(gson.toJson(new Argument("newPublisher", null, null, null)));
        when(publisherService.addPublisher("newPublisher")).thenReturn(true);

        String result = husksheetsController.handleRegister(req, res);
        Result expected = new Result(true, "Publisher registered", null);
        assertEquals(gson.toJson(expected), result);
    }

    @Test
    public void testGetPublishers() {
        Request req = mock(Request.class);
        Response res = mock(Response.class);

        HashMap<String, Publisher> publishers = new HashMap<>();
        publishers.put("publisher1", new Publisher("publisher1"));
        publishers.put("publisher2", new Publisher("publisher2"));

        when(publisherService.getPublishers()).thenReturn(publishers);

        String result = husksheetsController.handleGetPublishers(req, res);
        List<Argument> expectedPublishers = new ArrayList<>();
        expectedPublishers.add(new Argument("publisher2", null, null, null));
        expectedPublishers.add(new Argument("publisher1", null, null, null));
        Result expected = new Result(true, "Success", expectedPublishers);
        assertEquals(gson.toJson(expected), result);
    }

    @Test
    public void testCreateSheet() {
        Request req = mock(Request.class);
        Response res = mock(Response.class);

        when(req.body()).thenReturn(gson.toJson(new Argument("publisher", "newSheet", null, null)));
        when(publisherService.createSheet("publisher", "newSheet")).thenReturn(true);

        String result = husksheetsController.handleCreateSheet(req, res);
        Result expected = new Result(true, "Sheet created", null);
        assertEquals(gson.toJson(expected), result);
    }

    @Test
    public void testDeleteSheet() {
        Request req = mock(Request.class);
        Response res = mock(Response.class);

        when(req.body()).thenReturn(gson.toJson(new Argument("publisher", "sheetToDelete", null, null)));
        when(publisherService.deleteSheet("publisher", "sheetToDelete")).thenReturn(true);

        String result = husksheetsController.handleDeleteSheet(req, res);
        Result expected = new Result(true, "Sheet deleted", null);
        assertEquals(gson.toJson(expected), result);
    }

    @Test
    public void testGetSheets() {
        Request req = mock(Request.class);
        Response res = mock(Response.class);

        List<String> sheets = new ArrayList<>();
        sheets.add("sheet1");
        sheets.add("sheet2");

        when(req.body()).thenReturn(gson.toJson(new Argument("publisher", null, null, null)));
        when(publisherService.getSheets("publisher")).thenReturn(sheets);

        String result = husksheetsController.handleGetSheets(req, res);
        List<Argument> sheetArgs = new ArrayList<>();
        sheetArgs.add(new Argument("publisher", "sheet1", null, null));
        sheetArgs.add(new Argument("publisher", "sheet2", null, null));
        Result expected = new Result(true, "Success", sheetArgs);
        assertEquals(gson.toJson(expected), result);
    }

    @Test
    public void testGetUpdatesForSubscription() {
        Request req = mock(Request.class);
        Response res = mock(Response.class);

        List<Argument> updates = new ArrayList<>();
        updates.add(new Argument("publisher", "sheet", "1", "payload1"));
        updates.add(new Argument("publisher", "sheet", "2", "payload2"));

        when(req.body()).thenReturn(gson.toJson(new Argument("publisher", "sheet", "0", null)));
        when(publisherService.getUpdatesForSubscription("publisher", "sheet", "0")).thenReturn(updates);

        String result = husksheetsController.handleGetUpdatesForSubscription(req, res);
        StringBuilder payloadBuilder = new StringBuilder();
        for (Argument update : updates) {
            payloadBuilder.append(update.payload).append("\n");
        }
        Argument expectedArg = new Argument("publisher", "sheet", "2", payloadBuilder.toString().trim());
        Result expected = new Result(true, "Success", List.of(expectedArg));
        assertEquals(gson.toJson(expected), result);
    }

    @Test
    public void testGetUpdatesForPublished() {
        Request req = mock(Request.class);
        Response res = mock(Response.class);

        List<Argument> updates = new ArrayList<>();
        updates.add(new Argument("publisher", "sheet", "1", "payload1"));
        updates.add(new Argument("publisher", "sheet", "2", "payload2"));

        when(req.body()).thenReturn(gson.toJson(new Argument("publisher", "sheet", "0", null)));
        when(publisherService.getUpdatesForPublished("publisher", "sheet", "0")).thenReturn(updates);

        String result = husksheetsController.handleGetUpdatesForPublished(req, res);
        StringBuilder payloadBuilder = new StringBuilder();
        for (Argument update : updates) {
            payloadBuilder.append(update.payload).append("\n");
        }
        Argument expectedArg = new Argument("publisher", "sheet", "2", payloadBuilder.toString().trim());
        Result expected = new Result(true, "Success", List.of(expectedArg));
        assertEquals(gson.toJson(expected), result);
    }

    @Test
    public void testUpdatePublished() {
        Request req = mock(Request.class);
        Response res = mock(Response.class);

        when(req.body()).thenReturn(gson.toJson(new Argument("publisher", "sheet", null, "payload")));
        when(publisherService.updatePublished("publisher", "sheet", "payload")).thenReturn(true);

        String result = husksheetsController.handleUpdatePublished(req, res);
        Result expected = new Result(true, "Update published", null);
        assertEquals(gson.toJson(expected), result);
    }

    @Test
    public void testUpdateSubscription() {
        Request req = mock(Request.class);
        Response res = mock(Response.class);

        when(req.body()).thenReturn(gson.toJson(new Argument("publisher", "sheet", null, "payload")));
        when(publisherService.updateSubscription("publisher", "sheet", "payload")).thenReturn(true);

        String result = husksheetsController.handleUpdateSubscription(req, res);
        Result expected = new Result(true, "Update subscription", null);
        assertEquals(gson.toJson(expected), result);
    }
}
