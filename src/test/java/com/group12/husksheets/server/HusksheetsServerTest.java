// Owner: Silas Nevstad


package com.group12.husksheets.server;

import static org.junit.jupiter.api.Assertions.*;
import static spark.Spark.*;

import com.group12.husksheets.server.services.DatabaseService;
import org.junit.jupiter.api.*;
import com.google.gson.Gson;
import com.group12.husksheets.models.Argument;
import com.group12.husksheets.models.Result;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Base64;
import java.io.OutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import javax.net.ssl.HttpsURLConnection;

public class HusksheetsServerTest {

    private static final Gson gson = new Gson();

    @BeforeAll
    public static void setupClass() {
        setupTrustStore();
    }

    @BeforeEach
    public void setUp() {
        stop();
        awaitStop();
        clearDatabase();
        HusksheetsServer.startServer("jdbc:sqlite:husksheetsTest.db");
        waitForServerToStart();
    }

    private static void clearDatabase() {
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

    @AfterEach
    public void tearDown() {
        stop();
        awaitStop();
    }

    @Test
    public void testRegister() throws IOException {
        Argument arg = new Argument();
        arg.publisher = "testPublisher1";
        String jsonArg = gson.toJson(arg);

        HttpURLConnection connection = createConnection("/api/v1/register", "GET", null);
        int responseCode = connection.getResponseCode();
        assertEquals(200, responseCode);

        Result result = gson.fromJson(getResponse(connection), Result.class);
        assertTrue(result.success);
        assertEquals("Publisher registered", result.message);
    }

    @Disabled
    @Test
    public void testRegisterEmptyPublisher() throws IOException {
        HttpURLConnection connection = createConnection("/api/v1/register", "GET", null);
        int responseCode = connection.getResponseCode();
        assertEquals(200, responseCode);

        Result result = gson.fromJson(getResponse(connection), Result.class);
        assertFalse(result.success);
        assertEquals("Invalid publisher name", result.message);
    }

    @Test
    public void testRegisterExistingPublisher() throws IOException {
        Argument arg = new Argument();
        arg.publisher = "testPublisher1";
        String jsonArg = gson.toJson(arg);

        HttpURLConnection connection = createConnection("/api/v1/register", "GET", null);
        assertEquals(200, connection.getResponseCode());

        connection = createConnection("/api/v1/register", "GET", null);
        int responseCode = connection.getResponseCode();
        assertEquals(200, responseCode);

        Result result = gson.fromJson(getResponse(connection), Result.class);
        assertFalse(result.success);
        assertEquals("Publisher already exists", result.message);
    }

    @Test
    public void testGetPublishers() throws IOException {
        Argument registerArg = new Argument();
        registerArg.publisher = "testPublisher2";
        String registerJsonArg = gson.toJson(registerArg);

        HttpURLConnection registerConnection = createConnection("/api/v1/register", "GET", null);
        assertEquals(200, registerConnection.getResponseCode());

        HttpURLConnection connection = createConnection("/api/v1/getPublishers", "GET", null);
        int responseCode = connection.getResponseCode();
        assertEquals(200, responseCode);

        Result result = gson.fromJson(getResponse(connection), Result.class);
        assertTrue(result.success);
        assertFalse(result.value.isEmpty());
    }

    @Test
    public void testCreateSheet() throws IOException {
        Argument registerArg = new Argument();
        registerArg.publisher = "user1";
        String registerJsonArg = gson.toJson(registerArg);

        HttpURLConnection registerConnection = createConnection("/api/v1/register", "GET", null);
        assertEquals(200, registerConnection.getResponseCode());

        Argument createSheetArg = new Argument();
        createSheetArg.publisher = "user1";
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
    public void testCreateSheetEmptySheet() throws IOException {
        Argument registerArg = new Argument();
        registerArg.publisher = "testPublisher3";
        String registerJsonArg = gson.toJson(registerArg);

        HttpURLConnection registerConnection = createConnection("/api/v1/register", "GET", null);
        assertEquals(200, registerConnection.getResponseCode());

        Argument createSheetArg = new Argument();
        createSheetArg.publisher = "testPublisher3";
        createSheetArg.sheet = "";
        String createSheetJsonArg = gson.toJson(createSheetArg);

        HttpURLConnection connection = createConnection("/api/v1/createSheet", "POST", createSheetJsonArg);
        int responseCode = connection.getResponseCode();
        assertEquals(200, responseCode);

        Result result = gson.fromJson(getResponse(connection), Result.class);
        assertFalse(result.success);
        assertEquals("Invalid input", result.message);
    }

    @Test
    public void testDeleteSheet() throws IOException {
        Argument registerArg = new Argument();
        registerArg.publisher = "testPublisher4";
        String registerJsonArg = gson.toJson(registerArg);

        HttpURLConnection registerConnection = createConnection("/api/v1/register", "GET", null);
        assertEquals(200, registerConnection.getResponseCode());

        Argument createSheetArg = new Argument();
        createSheetArg.publisher = "user1";
        createSheetArg.sheet = "testSheet";
        String createSheetJsonArg = gson.toJson(createSheetArg);

        HttpURLConnection createSheetConnection = createConnection("/api/v1/createSheet", "POST", createSheetJsonArg);
        assertEquals(200, createSheetConnection.getResponseCode());

        Argument deleteSheetArg = new Argument();
        deleteSheetArg.publisher = "user1";
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
    public void testDeleteSheetEmptySheet() throws IOException {
        Argument registerArg = new Argument();
        registerArg.publisher = "testPublisher4";
        String registerJsonArg = gson.toJson(registerArg);

        HttpURLConnection registerConnection = createConnection("/api/v1/register", "GET", null);
        assertEquals(200, registerConnection.getResponseCode());

        Argument createSheetArg = new Argument();
        createSheetArg.publisher = "testPublisher4";
        createSheetArg.sheet = "testSheet";
        String createSheetJsonArg = gson.toJson(createSheetArg);

        HttpURLConnection createSheetConnection = createConnection("/api/v1/createSheet", "POST", createSheetJsonArg);
        assertEquals(200, createSheetConnection.getResponseCode());

        Argument deleteSheetArg = new Argument();
        deleteSheetArg.publisher = "testPublisher4";
        deleteSheetArg.sheet = "";
        String deleteSheetJsonArg = gson.toJson(deleteSheetArg);

        HttpURLConnection connection = createConnection("/api/v1/deleteSheet", "POST", deleteSheetJsonArg);
        int responseCode = connection.getResponseCode();
        assertEquals(200, responseCode);

        Result result = gson.fromJson(getResponse(connection), Result.class);
        assertFalse(result.success);
        assertEquals("Invalid input", result.message);
    }

    @Test
    public void testGetSheets() throws IOException {
        HttpURLConnection registerConnection = createConnection("/api/v1/register", "GET", null);
        assertEquals(200, registerConnection.getResponseCode());

        Argument createSheetArg = new Argument();
        createSheetArg.publisher = "user1";
        createSheetArg.sheet = "testSheet";
        String createSheetJsonArg = gson.toJson(createSheetArg);

        HttpURLConnection createSheetConnection = createConnection("/api/v1/createSheet", "POST", createSheetJsonArg);
        assertEquals(200, createSheetConnection.getResponseCode());

        Argument getSheetsArg = new Argument();
        getSheetsArg.publisher = "user1";
        String getSheetsJsonArg = gson.toJson(getSheetsArg);

        HttpURLConnection connection = createConnection("/api/v1/getSheets", "POST", getSheetsJsonArg);
        int responseCode = connection.getResponseCode();
        assertEquals(200, responseCode);

        Result result = gson.fromJson(getResponse(connection), Result.class);
        assertTrue(result.success);
        assertFalse(result.value.isEmpty());
        assertEquals("testSheet", result.value.get(0).sheet);
    }

    @Test
    public void testGetSheetsEmptyPublisher() throws IOException {
        Argument getSheetsArg = new Argument();
        getSheetsArg.publisher = "";
        String getSheetsJsonArg = gson.toJson(getSheetsArg);

        HttpURLConnection connection = createConnection("/api/v1/getSheets", "POST", getSheetsJsonArg);
        int responseCode = connection.getResponseCode();
        assertEquals(200, responseCode);

        Result result = gson.fromJson(getResponse(connection), Result.class);
        assertFalse(result.success);
        assertEquals("Invalid input", result.message);
    }

    private HttpURLConnection createConnection(String endpoint, String method, String jsonBody) throws IOException {
        URL url = new URL("https://localhost:9443" + endpoint);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString("user1:password1".getBytes(StandardCharsets.UTF_8)));
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

    private static void setupTrustStore() {
        String trustStorePath = "src/main/resources/truststore.jks";
        String trustStorePassword = "husksheets";

        System.setProperty("javax.net.ssl.trustStore", trustStorePath);
        System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);
    }

    private void waitForServerToStart() {
        int retries = 10;
        while (retries-- > 0) {
            try {
                HttpURLConnection connection = createConnection("/api/v1/getPublishers", "GET", null);
                connection.getResponseCode();
                return; // Server is up
            } catch (IOException e) {
                try {
                    Thread.sleep(500); // Wait for 500 milliseconds before retrying
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted while waiting for server to start", interruptedException);
                }
            }
        }
        throw new RuntimeException("Failed to connect to the server after multiple retries");
    }
}