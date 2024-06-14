// Owner: Silas Nevstad

package com.group12.husksheets.server.controllers;

import static spark.Spark.*;

import com.group12.husksheets.models.Argument;
import com.group12.husksheets.models.Result;
import com.group12.husksheets.server.services.PublisherService;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import com.group12.husksheets.server.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

public class HusksheetsController {
    private static final Logger logger = LoggerFactory.getLogger(HusksheetsController.class);
    private final PublisherService publisherService;
    private final UserService userService;
    private final Gson gson = new Gson();

    /**
     * Constructor for HusksheetsController.
     * Sets up the controller with publisher and user services.
     *
     * @param publisherService The service for handling publisher-related operations.
     * @param userService The service for handling user-related (authentication) operations.
     */
    public HusksheetsController(PublisherService publisherService, UserService userService) {
        this.publisherService = publisherService;
        this.userService = userService;
        setupEndpoints();
    }

    /**
     * Sets up all the API endpoints and their corresponding handlers.
     */
    private void setupEndpoints() {
        // Middleware to check for valid authorization header
        before(this::beforeFilter);

        // API endpoints
        post("/api/v1/register", this::handleRegister);
        get("/api/v1/getPublishers", this::handleGetPublishers);
        post("/api/v1/createSheet", this::handleCreateSheet);
        post("/api/v1/deleteSheet", this::handleDeleteSheet);
        post("/api/v1/getSheets", this::handleGetSheets);
        post("/api/v1/getUpdatesForSubscription", this::handleGetUpdatesForSubscription);
        post("/api/v1/getUpdatesForPublished", this::handleGetUpdatesForPublished);
        post("/api/v1/updatePublished", this::handleUpdatePublished);
        post("/api/v1/updateSubscription", this::handleUpdateSubscription);
    }

    /**
     * Handles the registration of a new publisher.
     *
     * @param req The request object containing the registration data.
     * @param res The response object to be sent back to the client.
     * @return JSON response indicating the success or failure of the operation.
     */
    public String handleRegister(Request req, Response res) {
        Argument arg = gson.fromJson(req.body(), Argument.class);
        if (publisherService.isInvalidInput(arg.publisher)) {
            logger.warn("Invalid input for publisher registration: {}", arg.publisher);
            return gson.toJson(new Result(false, "Invalid publisher name", null));
        }
        boolean success = publisherService.addPublisher(arg.publisher);
        if (!success) {
            logger.warn("Publisher {} already exists", arg.publisher);
            return gson.toJson(new Result(false, "Publisher already exists", null));
        }
        logger.info("Publisher {} registered", arg.publisher);
        return gson.toJson(new Result(true, "Publisher registered", null));
    }

    /**
     * Handles the retrieval of all publishers.
     *
     * @param req The request object.
     * @param res The response object to be sent back to the client.
     * @return JSON response containing the list of publishers.
     */
    public String handleGetPublishers(Request req, Response res) {
        List<Argument> publisherList = new ArrayList<>();
        publisherService.getPublishers().forEach((name, publisher) -> {
            Argument arg = new Argument();
            arg.publisher = name;
            publisherList.add(arg);
        });
        Result result = new Result(true, "Success", publisherList);
        return gson.toJson(result);
    }

    /**
     * Handles the creation of a new sheet for a publisher.
     *
     * @param req The request object containing the sheet creation data.
     * @param res The response object to be sent back to the client.
     * @return JSON response indicating the success or failure of the operation.
     */
    public String handleCreateSheet(Request req, Response res) {
        Argument arg = gson.fromJson(req.body(), Argument.class);
        if (publisherService.isInvalidInput(arg.publisher, arg.sheet)) {
            logger.warn("Invalid input for sheet creation: publisher={}, sheet={}", arg.publisher, arg.sheet);
            return gson.toJson(new Result(false, "Invalid input", null));
        }
        boolean success = publisherService.createSheet(arg.publisher, arg.sheet);
        if (!success) {
            logger.warn("Sheet {} already exists for publisher {}", arg.sheet, arg.publisher);
            return gson.toJson(new Result(false, "Sheet already exists", null));
        }
        logger.info("Sheet {} created for publisher {}", arg.sheet, arg.publisher);
        return gson.toJson(new Result(true, "Sheet created", null));
    }

    /**
     * Handles the deletion of a sheet for a publisher.
     *
     * @param req The request object containing the sheet deletion data.
     * @param res The response object to be sent back to the client.
     * @return JSON response indicating the success or failure of the operation.
     */
    public String handleDeleteSheet(Request req, Response res) {
        Argument arg = gson.fromJson(req.body(), Argument.class);
        if (publisherService.isInvalidInput(arg.publisher, arg.sheet)) {
            logger.warn("Invalid input for sheet deletion: publisher={}, sheet={}", arg.publisher, arg.sheet);
            return gson.toJson(new Result(false, "Invalid input", null));
        }
        boolean success = publisherService.deleteSheet(arg.publisher, arg.sheet);
        if (!success) {
            logger.warn("Sheet {} does not exist for publisher {}", arg.sheet, arg.publisher);
            return gson.toJson(new Result(false, "Sheet does not exist", null));
        }
        logger.info("Sheet {} deleted for publisher {}", arg.sheet, arg.publisher);
        return gson.toJson(new Result(true, "Sheet deleted", null));
    }

    /**
     * Handles the retrieval of all sheets for a publisher.
     *
     * @param req The request object containing the publisher data.
     * @param res The response object to be sent back to the client.
     * @return JSON response containing the list of sheets.
     */
    public String handleGetSheets(Request req, Response res) {
        Argument arg = gson.fromJson(req.body(), Argument.class);
        if (publisherService.isInvalidInput(arg.publisher)) {
            logger.warn("Invalid input for getting sheets: publisher={}", arg.publisher);
            return gson.toJson(new Result(false, "Invalid input", null));
        }
        List<String> sheets = publisherService.getSheets(arg.publisher);
        if (sheets == null) {
            logger.warn("Publisher {} does not exist", arg.publisher);
            return gson.toJson(new Result(false, "Publisher does not exist", null));
        }
        List<Argument> sheetList = new ArrayList<>();
        for (String sheet : sheets) {
            Argument sheetArg = new Argument();
            sheetArg.publisher = arg.publisher;
            sheetArg.sheet = sheet;
            sheetList.add(sheetArg);
        }
        Result result = new Result(true, "Success", sheetList);
        return gson.toJson(result);
    }

    /**
     * Handles the retrieval of updates for a subscribed sheet.
     *
     * @param req The request object containing the subscription data.
     * @param res The response object to be sent back to the client.
     * @return JSON response containing the list of updates.
     */
    public String handleGetUpdatesForSubscription(Request req, Response res) {
        Argument arg = gson.fromJson(req.body(), Argument.class);
        if (publisherService.isInvalidInput(arg.publisher, arg.sheet)) {
            logger.warn("Invalid input for getting updates for subscription: publisher={}, sheet={}", arg.publisher, arg.sheet);
            return gson.toJson(new Result(false, "Invalid input", null));
        }
        List<Argument> updates = publisherService.getUpdatesForSubscription(arg.publisher, arg.sheet, arg.id);
        return makeUpdatesPayloadResp(arg, updates);
    }

    /**
     * Handles the retrieval of updates for a published sheet.
     *
     * @param req The request object containing the publication data.
     * @param res The response object to be sent back to the client.
     * @return JSON response containing the list of updates.
     */
    public String handleGetUpdatesForPublished(Request req, Response res) {
        Argument arg = gson.fromJson(req.body(), Argument.class);
        if (publisherService.isInvalidInput(arg.publisher, arg.sheet)) {
            logger.warn("Invalid input for getting updates for published: publisher={}, sheet={}", arg.publisher, arg.sheet);
            return gson.toJson(new Result(false, "Invalid input", null));
        }
        List<Argument> updates = publisherService.getUpdatesForPublished(arg.publisher, arg.sheet, arg.id);
        return makeUpdatesPayloadResp(arg, updates);
    }

    /**
     * Handles the update of a published sheet.
     *
     * @param req The request object containing the update data.
     * @param res The response object to be sent back to the client.
     * @return JSON response indicating the success or failure of the operation.
     */
    public String handleUpdatePublished(Request req, Response res) {
        Argument arg = gson.fromJson(req.body(), Argument.class);
        if (publisherService.isInvalidInput(arg.publisher, arg.sheet, arg.payload)) {
            logger.warn("Invalid input for updating published: publisher={}, sheet={}", arg.publisher, arg.sheet);
            return gson.toJson(new Result(false, "Invalid input", null));
        }
        boolean success = publisherService.updatePublished(arg.publisher, arg.sheet, arg.payload);
        return gson.toJson(new Result(
                success,
                success ? "Update published" : "Publisher or sheet does not exist",
                null
        ));
    }

    /**
     * Handles the update of a subscription for a sheet.
     *
     * @param req The request object containing the subscription update data.
     * @param res The response object to be sent back to the client.
     * @return JSON response indicating the success or failure of the operation.
     */
    public String handleUpdateSubscription(Request req, Response res) {
        Argument arg = gson.fromJson(req.body(), Argument.class);
        if (publisherService.isInvalidInput(arg.publisher, arg.sheet, arg.payload)) {
            logger.warn("Invalid input for updating subscriptions: publisher={}, sheet={}", arg.publisher, arg.sheet);
            return gson.toJson(new Result(false, "Invalid input", null));
        }
        boolean success = publisherService.updateSubscription(arg.publisher, arg.sheet, arg.payload);
        return gson.toJson(new Result(
                success,
                success ? "Update subscription" : "Publisher or sheet does not exist",
                null
        ));
    }

    /**
     * Creates a JSON response for the updates' payload.
     *
     * @param arg The argument containing the initial request data.
     * @param updates The list of updates to be included in the response.
     * @return JSON response containing the updates' payload.
     */
    private String makeUpdatesPayloadResp(Argument arg, List<Argument> updates) {
        String lastId = "";
        String payload = "";
        if (!updates.isEmpty()) {
            lastId = updates.get(updates.size() - 1).id;
            StringBuilder payloadBuilder = new StringBuilder();
            for (Argument update : updates) {
                payloadBuilder.append(update.payload);
            }
            payload = payloadBuilder.toString().trim();
        }
        Argument resultArg = new Argument();
        resultArg.publisher = arg.publisher;
        resultArg.sheet = arg.sheet;
        resultArg.id = lastId;
        resultArg.payload = payload;
        return gson.toJson(new Result(true, "Success", List.of(resultArg)));
    }

    /**
     * Middleware to check for valid authorization header.
     *
     * @param request The request object.
     * @param response The response object.
     */
    public void beforeFilter(Request request, Response response) {
        String authHeader = request.headers("Authorization");
        if (authHeader == null || !userService.isValidAuth(authHeader)) {
            halt(401, "Unauthorized");
        }
    }
}
