package com.group12.husksheets;

import static org.junit.jupiter.api.Assertions.*;
import static spark.Spark.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import com.google.gson.Gson;
import com.group12.husksheets.models.Argument;
import com.group12.husksheets.models.Result;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.io.OutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import javax.net.ssl.HttpsURLConnection;

public class HusksheetsServerTest {

    private static final Gson gson = new Gson();

    @BeforeAll
    public static void setUp() throws Exception {
        HusksheetsServer.main(null);
        setupTrustStore();
    }

    @AfterAll
    public static void tearDown() {
        stop();
    }

    @Test
    public void testRegister() throws IOException {
        Argument arg = new Argument();
        arg.publisher = "testPublisher1";
        String jsonArg = gson.toJson(arg);

        HttpURLConnection connection = createConnection("/api/v1/register", "POST", jsonArg);
        int responseCode = connection.getResponseCode();
        assertEquals(200, responseCode);

        Result result = gson.fromJson(getResponse(connection), Result.class);
        assertTrue(result.success);
        assertEquals("Publisher registered", result.message);
    }

    @Test
    public void testGetPublishers() throws IOException {
        Argument registerArg = new Argument();
        registerArg.publisher = "testPublisher2";
        String registerJsonArg = gson.toJson(registerArg);

        HttpURLConnection registerConnection = createConnection("/api/v1/register", "POST", registerJsonArg);
        assertEquals(200, registerConnection.getResponseCode());

        HttpURLConnection connection = createConnection("/api/v1/getPublishers", "GET", null);
        int responseCode = connection.getResponseCode();
        assertEquals(200, responseCode);

        Result result = gson.fromJson(getResponse(connection), Result.class);
        assertTrue(result.success);
        assertFalse(result.value.isEmpty());
//        assertEquals("testPublisher2", result.value.get(0).publisher);
    }

    @Test
    public void testCreateSheet() throws IOException {
        Argument registerArg = new Argument();
        registerArg.publisher = "testPublisher3";
        String registerJsonArg = gson.toJson(registerArg);

        HttpURLConnection registerConnection = createConnection("/api/v1/register", "POST", registerJsonArg);
        assertEquals(200, registerConnection.getResponseCode());

        Argument createSheetArg = new Argument();
        createSheetArg.publisher = "testPublisher3";
        createSheetArg.sheet = "testSheet";
        String createSheetJsonArg = gson.toJson(createSheetArg);

        HttpURLConnection connection = createConnection("/api/v1/createSheet", "POST", createSheetJsonArg);
        int responseCode = connection.getResponseCode();
        assertEquals(200, responseCode);

        Result result = gson.fromJson(getResponse(connection), Result.class);
        assertTrue(result.success);
        assertEquals("Sheet created", result.message);
    }

    @Test
    public void testDeleteSheet() throws IOException {
        Argument registerArg = new Argument();
        registerArg.publisher = "testPublisher4";
        String registerJsonArg = gson.toJson(registerArg);

        HttpURLConnection registerConnection = createConnection("/api/v1/register", "POST", registerJsonArg);
        assertEquals(200, registerConnection.getResponseCode());

        Argument createSheetArg = new Argument();
        createSheetArg.publisher = "testPublisher4";
        createSheetArg.sheet = "testSheet";
        String createSheetJsonArg = gson.toJson(createSheetArg);

        HttpURLConnection createSheetConnection = createConnection("/api/v1/createSheet", "POST", createSheetJsonArg);
        assertEquals(200, createSheetConnection.getResponseCode());

        Argument deleteSheetArg = new Argument();
        deleteSheetArg.publisher = "testPublisher4";
        deleteSheetArg.sheet = "testSheet";
        String deleteSheetJsonArg = gson.toJson(deleteSheetArg);

        HttpURLConnection connection = createConnection("/api/v1/deleteSheet", "POST", deleteSheetJsonArg);
        int responseCode = connection.getResponseCode();
        assertEquals(200, responseCode);

        Result result = gson.fromJson(getResponse(connection), Result.class);
        assertTrue(result.success);
        assertEquals("Sheet deleted", result.message);
    }

    @Test
    public void testGetSheets() throws IOException {
        Argument registerArg = new Argument();
        registerArg.publisher = "testPublisher5";
        String registerJsonArg = gson.toJson(registerArg);

        HttpURLConnection registerConnection = createConnection("/api/v1/register", "POST", registerJsonArg);
        assertEquals(200, registerConnection.getResponseCode());

        Argument createSheetArg = new Argument();
        createSheetArg.publisher = "testPublisher5";
        createSheetArg.sheet = "testSheet";
        String createSheetJsonArg = gson.toJson(createSheetArg);

        HttpURLConnection createSheetConnection = createConnection("/api/v1/createSheet", "POST", createSheetJsonArg);
        assertEquals(200, createSheetConnection.getResponseCode());

        Argument getSheetsArg = new Argument();
        getSheetsArg.publisher = "testPublisher5";
        String getSheetsJsonArg = gson.toJson(getSheetsArg);

        HttpURLConnection connection = createConnection("/api/v1/getSheets", "POST", getSheetsJsonArg);
        int responseCode = connection.getResponseCode();
        assertEquals(200, responseCode);

        Result result = gson.fromJson(getResponse(connection), Result.class);
        assertTrue(result.success);
        assertFalse(result.value.isEmpty());
        assertEquals("testSheet", result.value.get(0).sheet);
    }

    private HttpURLConnection createConnection(String endpoint, String method, String jsonBody) throws IOException {
        URL url = new URL("https://localhost:9443" + endpoint);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString("testPublisher:testPublisher".getBytes(StandardCharsets.UTF_8)));
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        if (jsonBody != null) {
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
        }
        return connection;
    }

    private String getResponse(HttpURLConnection connection) throws IOException {
        try (InputStream is = connection.getInputStream()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static void setupTrustStore() throws Exception {
        String trustStorePath = "src/main/resources/truststore.jks";
        String trustStorePassword = "husksheets";

        System.setProperty("javax.net.ssl.trustStore", trustStorePath);
        System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);
    }
}