// Owner: Silas Nevstad

package com.group12.husksheets.ui.services;

import com.google.gson.Gson;
import com.group12.husksheets.models.Argument;
import com.group12.husksheets.models.Result;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

public class BackendService {
    private final String BASE_URL = "https://localhost:9443/api/v1";
//    private final String BASE_URL = "https://husksheets.fly.dev:443/api/v1";
    private final Gson gson = new Gson();
    private final String username;
    private final String password;

    /**
     * Constructs a BackendService object (used for making API requests to the server)
     *
     * @param username The username to authenticate with
     * @param password The password to authenticate with
     */
    public BackendService(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Registers a new publisher
     *
     * @return The result of the registration
     * @throws Exception If the request fails
     */
    public Result register() throws Exception {
        String response = getRequest("/register");
        return gson.fromJson(response, Result.class);
    }

    /**
     * Gets a list of all registered publishers
     *
     * @return The result containing the list of publishers
     * @throws Exception If the request fails
     */
    public Result getPublishers() throws Exception {
        String response = getRequest("/getPublishers");
        return gson.fromJson(response, Result.class);
    }

    /**
     * Checks if a publisher exists
     *
     * @param publisher The name of the publisher to check
     * @return Whether the publisher exists
     * @throws Exception If the request fails
     */
    public boolean doesPublisherExist(String publisher) throws Exception {
        Result result = getPublishers();
        for (Argument arg : result.value) {
            if (arg.publisher.equals(publisher)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates a new sheet for a publisher
     *
     * @param publisher The publisher to create the sheet for
     * @param sheet     The name of the sheet to create
     * @return The result of the creation
     * @throws Exception If the request fails
     */
    public Result createSheet(String publisher, String sheet) throws Exception {
        Argument arg = new Argument(publisher, sheet, null, null);
        String response = postRequest("/createSheet", arg);
        return gson.fromJson(response, Result.class);
    }

    /**
     * Deletes a sheet for a publisher
     *
     * @param publisher The publisher to delete the sheet for
     * @param sheet     The name of the sheet to delete
     * @return The result of the deletion
     * @throws Exception If the request fails
     */
    public Result deleteSheet(String publisher, String sheet) throws Exception {
        Argument arg = new Argument(publisher, sheet, null, null);
        String response = postRequest("/deleteSheet", arg);
        return gson.fromJson(response, Result.class);
    }

    /**
     * Gets a list of all sheets for a publisher
     *
     * @param publisher The publisher to get the sheets for
     * @return The result containing the list of sheets
     * @throws Exception If the request fails
     */
    public Result getSheets(String publisher) throws Exception {
        Argument arg = new Argument(publisher, null, null, null);
        String response = postRequest("/getSheets", arg);
        return gson.fromJson(response, Result.class);
    }

    /**
     * Gets all sheets from all publishers
     *
     * @return The result containing the list of all sheets
     * @throws Exception If the request fails
     */
    public Result getAllSheets() throws Exception {
        Result publishersResult = getPublishers();
        List<Argument> publishers = publishersResult.value;
        List<Argument> allSheets = new java.util.ArrayList<>(List.of());
        for (Argument publisher : publishers) {
            Result sheetsResult = getSheets(publisher.publisher);
            allSheets.addAll(sheetsResult.value);
        }
        return new Result(true, null, allSheets);
    }

    /**
     * Gets the data for a sheet
     *
     * @param publisher The publisher to get the sheet data for (owner of the sheet)
     * @param sheet The name of the sheet to get the data for
     * @param id The last update id
     * @param isOwned Whether the sheet is owned by the publisher
     * @return The result containing the sheet data
     * @throws Exception If the request fails
     */
    public Result getUpdates(String publisher, String sheet, String id, boolean isOwned) throws Exception {
        if (isOwned) {
            return getUpdatesForPublished(publisher, sheet, id);
        } else {
            return getUpdatesForSubscription(publisher, sheet, id);
        }
    }

    /**
     * Gets all updates for a sheet
     *
     * @param publisher The publisher to get the sheet data for (owner of the sheet)
     * @param sheet The name of the sheet to get the data for
     * @param id The last update id
     * @return The result containing the sheet data
     * @throws Exception If the request fails
     */
    public Result getAllUpdates(String publisher, String sheet, String id) throws Exception {
        Result subscriberUpdatesRes = getUpdatesForPublished(publisher, sheet, id);
        Result publisherUpdatesRes = getUpdatesForSubscription(publisher, sheet, id);

        if (!subscriberUpdatesRes.success || !publisherUpdatesRes.success) {
            return new Result(false, null, null);
        }

        Argument subscriberUpdates = null;
        Argument publisherUpdates = null;

        if (!subscriberUpdatesRes.value.isEmpty()) {
            subscriberUpdates = subscriberUpdatesRes.value.get(0);
        }

        if (!publisherUpdatesRes.value.isEmpty()) {
            publisherUpdates = publisherUpdatesRes.value.get(0);
        }

        String combinedPayload = "";
        if (subscriberUpdates != null && subscriberUpdates.payload != null) {
            combinedPayload += subscriberUpdates.payload;
        }
        if (publisherUpdates != null && publisherUpdates.payload != null) {
            combinedPayload += publisherUpdates.payload;
        }

        String lastId = getLastId(subscriberUpdates, publisherUpdates);

        return new Result(true, lastId, List.of(new Argument(publisher, sheet, lastId, combinedPayload)));
    }

    /**
     * Gets the last update id from two updates
     *
     * @param subscriberUpdates updates from the subscriber
     * @param publisherUpdates updates from the publisher
     * @return
     */
    private static String getLastId(Argument subscriberUpdates, Argument publisherUpdates) {
        String lastId = null;
        if (subscriberUpdates != null && publisherUpdates != null) {
            if (subscriberUpdates.id != null && publisherUpdates.id != null) {
                lastId = subscriberUpdates.id.compareTo(publisherUpdates.id) > 0 ? subscriberUpdates.id : publisherUpdates.id;
            } else if (subscriberUpdates.id != null) {
                lastId = subscriberUpdates.id;
            } else if (publisherUpdates.id != null) {
                lastId = publisherUpdates.id;
            }
        } else if (subscriberUpdates != null && subscriberUpdates.id != null) {
            lastId = subscriberUpdates.id;
        } else if (publisherUpdates != null && publisherUpdates.id != null) {
            lastId = publisherUpdates.id;
        }
        return lastId;
    }

    /**
     * Gets the updates for a subscription
     *
     * @param publisher The publisher to get the sheet data for (owner of the sheet)
     * @param sheet     The name of the sheet to get the data for
     * @param id        The last update id
     * @return The result containing the sheet data
     * @throws Exception If the request fails
     */
    public Result getUpdatesForSubscription(String publisher, String sheet, String id) throws Exception {
        Argument arg = new Argument(publisher, sheet, id, null);
        String response = postRequest("/getUpdatesForSubscription", arg);
        return gson.fromJson(response, Result.class);
    }

    /**
     * Gets the updates for a published sheet
     *
     * @param publisher The publisher to get the sheet data for (owner of the sheet)
     * @param sheet     The name of the sheet to get the data for
     * @param id        The last update id
     * @return The result containing the sheet data
     * @throws Exception If the request fails
     */
    public Result getUpdatesForPublished(String publisher, String sheet, String id) throws Exception {
        Argument arg = new Argument(publisher, sheet, id, null);
        String response = postRequest("/getUpdatesForPublished", arg);
        return gson.fromJson(response, Result.class);
    }

    /**
     * Updates a sheet with new data
     *
     * @param publisher The publisher to update the sheet data for
     * @param sheet The name of the sheet to update the data for
     * @param payload The new data to update the sheet with
     * @param isOwned Whether the sheet is owned by the publisher
     * @return The result of the update
     * @throws Exception If the request fails
     */
    public Result updateSheet(String publisher, String sheet, String payload, boolean isOwned) throws Exception {
        if (isOwned) {
            return updatePublished(publisher, sheet, payload);
        } else {
            return updateSubscription(publisher, sheet, payload);
        }
    }

    /**
     * Sends updates for a published sheet
     *
     * @param publisher The publisher to update the sheet data for
     * @param sheet     The name of the sheet to update the data for
     * @param payload   The new data to update the sheet with
     * @return The result of the update
     * @throws Exception If the request fails
     */
    public Result updatePublished(String publisher, String sheet, String payload) throws Exception {
        Argument arg = new Argument(publisher, sheet, null, payload);
        String response = postRequest("/updatePublished", arg);
        return gson.fromJson(response, Result.class);
    }

    /**
     * Sends updates for a subscription
     *
     * @param publisher The publisher to update the sheet data for
     * @param sheet     The name of the sheet to update the data for
     * @param payload   The new data to update the sheet with
     * @return The result of the update
     * @throws Exception If the request fails
     */
    public Result updateSubscription(String publisher, String sheet, String payload) throws Exception {
        Argument arg = new Argument(publisher, sheet, null, payload);
        String response = postRequest("/updateSubscription", arg);
        return gson.fromJson(response, Result.class);
    }

    /**
     * Sends a GET request to the server
     *
     * @param endpoint The endpoint to send the request to
     * @return The response from the server
     * @throws Exception If the request fails
     */
    private String getRequest(String endpoint) throws Exception {
        URL url = new URL(BASE_URL + endpoint);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", getAuthHeader());

        return getResponse(connection);
    }

    /**
     * Sends a POST request to the server
     *
     * @param endpoint The endpoint to send the request to
     * @param arg The argument to send with the request
     * @return The response from the server
     * @throws Exception If the request fails
     */
    private String postRequest(String endpoint, Argument arg) throws Exception {
        URL url = new URL(BASE_URL + endpoint);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", getAuthHeader());
        connection.setDoOutput(true);

        String jsonInputString = gson.toJson(arg);
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        return getResponse(connection);
    }

    /**
     * Gets the response from an HTTP connection
     *
     * @param connection The connection to get the response from
     * @return The response from the server
     * @throws Exception If the request fails
     */
    private String getResponse(HttpURLConnection connection) throws Exception {
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)
            )) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                return response.toString();
            }
        } else {
            throw new Exception("HTTP request failed with response code " + responseCode);
        }
    }

    /**
     * Gets the authorization header for the request
     *
     * @return The authorization header
     */
    private String getAuthHeader() {
        String credentials = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
    }
}
