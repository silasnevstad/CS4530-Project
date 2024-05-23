package com.group12.husksheets.controllers;

import static spark.Spark.*;

import com.group12.husksheets.models.Argument;
import com.group12.husksheets.models.Result;
import com.group12.husksheets.services.PublisherService;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HusksheetsController {
    private static final Logger logger = LoggerFactory.getLogger(HusksheetsController.class);
    private final PublisherService publisherService;
    private final Gson gson = new Gson();

    public HusksheetsController(PublisherService publisherService) {
        this.publisherService = publisherService;
        setupEndpoints();
    }

    private void setupEndpoints() {
        before((request, response) -> {
            String authHeader = request.headers("Authorization");
            if (authHeader == null || !isValidUser(authHeader)) {
                halt(401, "Unauthorized");
            }
        });

        post("/api/v1/register", (req, res) -> {
            Argument arg = gson.fromJson(req.body(), Argument.class);
            boolean success = publisherService.addPublisher(arg.publisher);
            if (!success) {
                logger.warn("Publisher {} already exists", arg.publisher);
                return gson.toJson(new Result(false, "Publisher already exists", null));
            }
            logger.info("Publisher {} registered", arg.publisher);
            return gson.toJson(new Result(true, "Publisher registered", null));
        });

        get("/api/v1/getPublishers", (req, res) -> {
            List<Argument> publisherList = new ArrayList<>();
            publisherService.getPublishers().forEach((name, publisher) -> {
                Argument arg = new Argument();
                arg.publisher = name;
                publisherList.add(arg);
            });
            Result result = new Result(true, "Success", publisherList);
            return gson.toJson(result);
        });

        post("/api/v1/createSheet", (req, res) -> {
            Argument arg = gson.fromJson(req.body(), Argument.class);
            boolean success = publisherService.createSheet(arg.publisher, arg.sheet);
            if (!success) {
                logger.warn("Sheet {} already exists for publisher {}", arg.sheet, arg.publisher);
                return gson.toJson(new Result(false, "Sheet already exists", null));
            }
            logger.info("Sheet {} created for publisher {}", arg.sheet, arg.publisher);
            return gson.toJson(new Result(true, "Sheet created", null));
        });

        post("/api/v1/deleteSheet", (req, res) -> {
            Argument arg = gson.fromJson(req.body(), Argument.class);
            boolean success = publisherService.deleteSheet(arg.publisher, arg.sheet);
            if (!success) {
                logger.warn("Sheet {} does not exist for publisher {}", arg.sheet, arg.publisher);
                return gson.toJson(new Result(false, "Sheet does not exist", null));
            }
            logger.info("Sheet {} deleted for publisher {}", arg.sheet, arg.publisher);
            return gson.toJson(new Result(true, "Sheet deleted", null));
        });

        post("/api/v1/getSheets", (req, res) -> {
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
        });
    }

    private boolean isValidUser(String authHeader) {
        String decodedAuth = new String(Base64.getDecoder().decode(authHeader.replace("Basic ", "")));
        String[] creds = decodedAuth.split(":");
        String username = creds[0];
        String password = creds[1];

        return username.equals(password); // TODO: Implement proper user authentication
    }
}
