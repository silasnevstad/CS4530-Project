// Owner: Silas Nevstad

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
import spark.HaltException;
import spark.Request;
import spark.Response;
import spark.Spark;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    private void simulateRequestWithMiddleware(Request request, Response response, Runnable endpointHandler) {
        husksheetsController.beforeFilter(request, response);
        endpointHandler.run();
    }

    @Test
    public void testRegisterPublisherSuccess() {
        Request req = mock(Request.class);
        Response res = mock(Response.class);

        when(req.body()).thenReturn(gson.toJson(new Argument("newPublisher", null, null, null)));
        when(publisherService.addPublisher("newPublisher")).thenReturn(true);

        String result = husksheetsController.handleRegister(req, res);
        Result expected = new Result(true, "Publisher registered", null);
        assertEquals(gson.toJson(expected), result);
    }

    @Test
    public void testRegisterPublisherAlreadyExists() {
        Request req = mock(Request.class);
        Response res = mock(Response.class);

        when(req.body()).thenReturn(gson.toJson(new Argument("existingPublisher", null, null, null)));
        when(publisherService.addPublisher("existingPublisher")).thenReturn(false);

        String result = husksheetsController.handleRegister(req, res);
        Result expected = new Result(false, "Publisher already exists", null);
        assertEquals(gson.toJson(expected), result);
    }

    @Test
    public void testRegisterPublisherEmptyString() {
        Request req = mock(Request.class);
        Response res = mock(Response.class);

        Argument argument = new Argument("", null, null, null);
        when(req.body()).thenReturn(gson.toJson(argument));
        when(publisherService.isInvalidInput(argument.publisher)).thenReturn(true);

        String result = husksheetsController.handleRegister(req, res);
        Result expected = new Result(false, "Invalid publisher name", null);
        assertEquals(gson.toJson(expected), result);
    }

    @Test
    public void testRegisterPublisherNull() {
        Request req = mock(Request.class);
        Response res = mock(Response.class);

        Argument argument = new Argument(null, null, null, null);
        when(req.body()).thenReturn(gson.toJson(argument));
        when(publisherService.isInvalidInput(argument.publisher)).thenReturn(true);

        String result = husksheetsController.handleRegister(req, res);
        Result expected = new Result(false, "Invalid publisher name", null);
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
    public void testCreateSheetAlreadyExists() {
        Request req = mock(Request.class);
        Response res = mock(Response.class);

        when(req.body()).thenReturn(gson.toJson(new Argument("publisher", "existingSheet", null, null)));
        when(publisherService.createSheet("publisher", "existingSheet")).thenReturn(false);

        String result = husksheetsController.handleCreateSheet(req, res);
        Result expected = new Result(false, "Sheet already exists", null);
        assertEquals(gson.toJson(expected), result);
    }

    @Test
    public void testCreateSheetEmptyString() {
        Request req = mock(Request.class);
        Response res = mock(Response.class);

        Argument argument = new Argument("publisher", "", null, null);
        when(req.body()).thenReturn(gson.toJson(argument));
        when(publisherService.isInvalidInput(argument.publisher, argument.sheet)).thenReturn(true);

        String result = husksheetsController.handleCreateSheet(req, res);
        Result expected = new Result(false, "Invalid input", null);
        assertEquals(gson.toJson(expected), result);
    }

    @Test
    public void testCreateSheetNull() {
        Request req = mock(Request.class);
        Response res = mock(Response.class);

        Argument argument = new Argument("publisher", null, null, null);
        when(req.body()).thenReturn(gson.toJson(argument));
        when(publisherService.isInvalidInput(argument.publisher, argument.sheet)).thenReturn(true);

        String result = husksheetsController.handleCreateSheet(req, res);
        Result expected = new Result(false, "Invalid input", null);
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
    public void testDeleteSheetDoesNotExist() {
        Request req = mock(Request.class);
        Response res = mock(Response.class);

        when(req.body()).thenReturn(gson.toJson(new Argument("publisher", "nonexistentSheet", null, null)));
        when(publisherService.deleteSheet("publisher", "nonexistentSheet")).thenReturn(false);

        String result = husksheetsController.handleDeleteSheet(req, res);
        Result expected = new Result(false, "Sheet does not exist", null);
        assertEquals(gson.toJson(expected), result);
    }

    @Test
    public void testDeleteSheetEmptyString() {
        Request req = mock(Request.class);
        Response res = mock(Response.class);

        when(req.body()).thenReturn(gson.toJson(new Argument("publisher", "", null, null)));
        when(publisherService.isInvalidInput("publisher", "")).thenReturn(true);

        String result = husksheetsController.handleDeleteSheet(req, res);
        Result expected = new Result(false, "Invalid input", null);
        assertEquals(gson.toJson(expected), result);
    }

    @Test
    public void testDeleteSheetNull() {
        Request req = mock(Request.class);
        Response res = mock(Response.class);

        when(req.body()).thenReturn(gson.toJson(new Argument("publisher", null, null, null)));
        when(publisherService.isInvalidInput("publisher", null)).thenReturn(true);

        String result = husksheetsController.handleDeleteSheet(req, res);
        Result expected = new Result(false, "Invalid input", null);
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
    public void testGetSheetsNoSheets() {
        Request req = mock(Request.class);
        Response res = mock(Response.class);

        when(req.body()).thenReturn(gson.toJson(new Argument("publisher", null, null, null)));
        when(publisherService.getSheets("publisher")).thenReturn(null);

        String result = husksheetsController.handleGetSheets(req, res);
        Result expected = new Result(false, "Publisher does not exist", null);
        assertEquals(gson.toJson(expected), result);
    }

    @Test
    public void testGetSheetsEmptyString() {
        Request req = mock(Request.class);
        Response res = mock(Response.class);

        when(req.body()).thenReturn(gson.toJson(new Argument("", null, null, null)));
        when(publisherService.isInvalidInput("")).thenReturn(true);

        String result = husksheetsController.handleGetSheets(req, res);
        Result expected = new Result(false, "Invalid input", null);
        assertEquals(gson.toJson(expected), result);
    }

    @Test
    public void testGetSheetsNull() {
        Request req = mock(Request.class);
        Response res = mock(Response.class);

        when(req.body()).thenReturn(gson.toJson(new Argument(null, null, null, null)));
        when(publisherService.isInvalidInput((String) null)).thenReturn(true);

        String result = husksheetsController.handleGetSheets(req, res);
        Result expected = new Result(false, "Invalid input", null);
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
    public void testGetUpdatesForSubscriptionNoUpdates() {
        Request req = mock(Request.class);
        Response res = mock(Response.class);

        List<Argument> updates = new ArrayList<>();

        when(req.body()).thenReturn(gson.toJson(new Argument("publisher", "sheet", "0", null)));
        when(publisherService.getUpdatesForSubscription("publisher", "sheet", "0")).thenReturn(updates);

        String result = husksheetsController.handleGetUpdatesForSubscription(req, res);
        Argument expectedArg = new Argument("publisher", "sheet", "", "");
        Result expected = new Result(true, "Success", List.of(expectedArg));
        assertEquals(gson.toJson(expected), result);
    }

    @Test
    public void testGetUpdatesForSubscriptionEmptyString() {
        Request req = mock(Request.class);
        Response res = mock(Response.class);

        List<Argument> updates = new ArrayList<>();

        when(req.body()).thenReturn(gson.toJson(new Argument("", "", null, null)));
        when(publisherService.isInvalidInput("", "")).thenReturn(true);

        String result = husksheetsController.handleGetUpdatesForSubscription(req, res);
        Result expected = new Result(false, "Invalid input", null);
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
    public void testGetUpdatesForPublishedEmptyString() {
        Request req = mock(Request.class);
        Response res = mock(Response.class);

        when(req.body()).thenReturn(gson.toJson(new Argument("", "", "0", null)));
        when(publisherService.isInvalidInput("", "")).thenReturn(true);

        String result = husksheetsController.handleGetUpdatesForPublished(req, res);
        Result expected = new Result(false, "Invalid input", null);
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
    public void testUpdatePublishedPublisherSheetDoesNotExist() {
        Request req = mock(Request.class);
        Response res = mock(Response.class);

        when(req.body()).thenReturn(gson.toJson(new Argument("publisher", "nonexistentSheet", null, "payload")));
        when(publisherService.updatePublished("publisher", "nonexistentSheet", "payload")).thenReturn(false);

        String result = husksheetsController.handleUpdatePublished(req, res);
        Result expected = new Result(false, "Publisher or sheet does not exist", null);
        assertEquals(gson.toJson(expected), result);
    }

    @Test
    public void testUpdatePublishedEmptyString() {
        Request req = mock(Request.class);
        Response res = mock(Response.class);

        when(req.body()).thenReturn(gson.toJson(new Argument("publisher", "sheet", null, "")));
        when(publisherService.isInvalidInput("publisher", "sheet", "")).thenReturn(true);

        String result = husksheetsController.handleUpdatePublished(req, res);
        Result expected = new Result(false, "Invalid input", null);
        assertEquals(gson.toJson(expected), result);
    }

    @Test
    public void testUpdatePublishedNull() {
        Request req = mock(Request.class);
        Response res = mock(Response.class);

        when(req.body()).thenReturn(gson.toJson(new Argument("publisher", "sheet", null, null)));
        when(publisherService.isInvalidInput("publisher", "sheet", null)).thenReturn(true);

        String result = husksheetsController.handleUpdatePublished(req, res);
        Result expected = new Result(false, "Invalid input", null);
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

    @Test
    public void testUpdateSubscriptionPublisherSheetDoesNotExist() {
        Request req = mock(Request.class);
        Response res = mock(Response.class);

        when(req.body()).thenReturn(gson.toJson(new Argument("publisher", "nonexistentSheet", null, "payload")));
        when(publisherService.updateSubscription("publisher", "nonexistentSheet", "payload")).thenReturn(false);

        String result = husksheetsController.handleUpdateSubscription(req, res);
        Result expected = new Result(false, "Publisher or sheet does not exist", null);
        assertEquals(gson.toJson(expected), result);
    }

    @Test
    public void testUpdateSubscriptionEmptyString() {
        Request req = mock(Request.class);
        Response res = mock(Response.class);

        when(req.body()).thenReturn(gson.toJson(new Argument("publisher", "sheet", null, "")));
        when(publisherService.isInvalidInput("publisher", "sheet", "")).thenReturn(true);

        String result = husksheetsController.handleUpdateSubscription(req, res);
        Result expected = new Result(false, "Invalid input", null);
        assertEquals(gson.toJson(expected), result);
    }

    @Test
    public void testUpdateSubscriptionNull() {
        Request req = mock(Request.class);
        Response res = mock(Response.class);

        when(req.body()).thenReturn(gson.toJson(new Argument("publisher", "sheet", null, null)));
        when(publisherService.isInvalidInput("publisher", "sheet", null)).thenReturn(true);

        String result = husksheetsController.handleUpdateSubscription(req, res);
        Result expected = new Result(false, "Invalid input", null);
        assertEquals(gson.toJson(expected), result);
    }

    @Test
    public void testAuthorizationMiddlewareWithValidUser() {
        Request request = mock(Request.class);
        Response response = mock(Response.class);

        when(request.headers("Authorization")).thenReturn("valid-token");
        when(userService.isValidUser("valid-token")).thenReturn(true);
        when(request.body()).thenReturn(gson.toJson(new Argument("newPublisher", null, null, null)));

        simulateRequestWithMiddleware(request, response, () -> husksheetsController.handleRegister(request, response));

        verify(userService).isValidUser("valid-token");
        verify(response, never()).status(401);
    }

    @Test
    public void testAuthorizationMiddlewareWithInvalidUser() {
        Request request = mock(Request.class);
        Response response = mock(Response.class);

        when(request.headers("Authorization")).thenReturn("invalid-token");
        when(userService.isValidUser("invalid-token")).thenReturn(false);
        when(request.body()).thenReturn(gson.toJson(new Argument("newPublisher", null, null, null)));

        assertThrows(HaltException.class, () ->
                simulateRequestWithMiddleware(request, response, () -> husksheetsController.handleRegister(request, response))
        );

        verify(userService).isValidUser("invalid-token");
    }

    @Test
    public void testAuthorizationMiddlewareWithNoAuthHeader() {
        Request request = mock(Request.class);
        Response response = mock(Response.class);

        when(request.headers("Authorization")).thenReturn(null);
        when(request.body()).thenReturn(gson.toJson(new Argument("newPublisher", null, null, null)));

        assertThrows(HaltException.class, () ->
                simulateRequestWithMiddleware(request, response, () -> husksheetsController.handleRegister(request, response))
        );

        verify(userService, never()).isValidUser(anyString());
    }
}
