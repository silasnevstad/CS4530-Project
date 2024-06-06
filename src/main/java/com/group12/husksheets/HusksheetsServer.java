package com.group12.husksheets;

import static spark.Spark.*;

import com.group12.husksheets.controllers.HusksheetsController;
import com.group12.husksheets.services.PublisherService;

public class HusksheetsServer {

    public static void main(String[] args) {
        port(9443);

        secure("src/main/resources/keystore.jks", "husksheets", null, null);

        PublisherService publisherService = new PublisherService();
        new HusksheetsController(publisherService);
    }
}
