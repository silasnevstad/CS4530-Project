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

    public HusksheetsController(PublisherService publisherService, UserService userService) {
        this.publisherService = publisherService;
        this.userService = userService;
        setupEndpoints();
    }

    private void setupEndpoints() {
        before((request, response) -> {
            String authHeader = request.headers("Authorization");
            if (authHeader == null || !userService.isValidUser(authHeader)) {
                halt(401, "Unauthorized");
            }
        });

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

    public String handleRegister(Request req, Response res) {
        Argument arg = gson.fromJson(req.body(), Argument.class);
        boolean success = publisherService.addPublisher(arg.publisher);
        if (!success) {
            logger.warn("Publisher {} already exists", arg.publisher);
            return gson.toJson(new Result(false, "Publisher already exists", null));
        }
        logger.info("Publisher {} registered", arg.publisher);
        return gson.toJson(new Result(true, "Publisher registered", null));
    }

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

    public String handleCreateSheet(Request req, Response res) {
        Argument arg = gson.fromJson(req.body(), Argument.class);
        boolean success = publisherService.createSheet(arg.publisher, arg.sheet);
        if (!success) {
            logger.warn("Sheet {} already exists for publisher {}", arg.sheet, arg.publisher);
            return gson.toJson(new Result(false, "Sheet already exists", null));
        }
        logger.info("Sheet {} created for publisher {}", arg.sheet, arg.publisher);
        return gson.toJson(new Result(true, "Sheet created", null));
    }

    public String handleDeleteSheet(Request req, Response res) {
        Argument arg = gson.fromJson(req.body(), Argument.class);
        boolean success = publisherService.deleteSheet(arg.publisher, arg.sheet);
        if (!success) {
            logger.warn("Sheet {} does not exist for publisher {}", arg.sheet, arg.publisher);
            return gson.toJson(new Result(false, "Sheet does not exist", null));
        }
        logger.info("Sheet {} deleted for publisher {}", arg.sheet, arg.publisher);
        return gson.toJson(new Result(true, "Sheet deleted", null));
    }

    public String handleGetSheets(Request req, Response res) {
        Argument arg = gson.fromJson(req.body(), Argument.class);
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

    public String handleGetUpdatesForSubscription(Request req, Response res) {
        Argument arg = gson.fromJson(req.body(), Argument.class);
        List<Argument> updates = publisherService.getUpdatesForSubscription(arg.publisher, arg.sheet, arg.id);
        return makeUpdatesPayloadResp(arg, updates);
    }

    public String handleGetUpdatesForPublished(Request req, Response res) {
        Argument arg = gson.fromJson(req.body(), Argument.class);
        List<Argument> updates = publisherService.getUpdatesForPublished(arg.publisher, arg.sheet, arg.id);
        return makeUpdatesPayloadResp(arg, updates);
    }

    public String handleUpdatePublished(Request req, Response res) {
        Argument arg = gson.fromJson(req.body(), Argument.class);
        boolean success = publisherService.updatePublished(arg.publisher, arg.sheet, arg.payload);
        return gson.toJson(new Result(
                success,
                success ? "Update published" : "Publisher or sheet does not exist",
                null
        ));
    }

    public String handleUpdateSubscription(Request req, Response res) {
        Argument arg = gson.fromJson(req.body(), Argument.class);
        boolean success = publisherService.updateSubscription(arg.publisher, arg.sheet, arg.payload);
        return gson.toJson(new Result(
                success,
                success ? "Update subscription" : "Publisher or sheet does not exist",
                null
        ));
    }

    private String makeUpdatesPayloadResp(Argument arg, List<Argument> updates) {
        String lastId = "";
        String payload = "";
        if (!updates.isEmpty()) {
            lastId = updates.get(updates.size() - 1).id;
            StringBuilder payloadBuilder = new StringBuilder();
            for (Argument update : updates) {
                payloadBuilder.append(update.payload).append("\n");
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
}
