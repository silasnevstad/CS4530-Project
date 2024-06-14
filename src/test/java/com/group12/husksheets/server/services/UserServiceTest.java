// Owner: Silas Nevstad

package com.group12.husksheets.server.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

    private UserService userService;

    @BeforeEach
    public void setUp() {
        userService = new UserService();
    }

    private String encodeCredentials(String username, String password) {
        return "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
    }

    @Test
    public void testIsValidUserWithValidCredentials() {
        String authHeader = encodeCredentials("user1", "password1");
        assertTrue(userService.isValidAuth(authHeader));
    }

    @Test
    public void testIsValidAuthWithInvalidCredentials() {
        String authHeader = encodeCredentials("user1", "wrongpassword");
        assertFalse(userService.isValidAuth(authHeader));
    }

    @Test
    public void testIsValidUserWithNonExistentAuth() {
        String authHeader = encodeCredentials("nonexistent", "password");
        assertFalse(userService.isValidAuth(authHeader));
    }

    @Test
    public void testIsValidAuthWithMalformedHeader() {
        String authHeader = "Basic malformed";
        assertFalse(userService.isValidAuth(authHeader));
    }

    @Test
    public void testIsValidAuthWithEmptyHeader() {
        String authHeader = "";
        assertFalse(userService.isValidAuth(authHeader));
    }

    @Test
    public void testIsValidAuthWithNullHeader() {
        assertFalse(userService.isValidAuth(null));
    }

    @Test
    public void testIsValidAuthWithInvalidBase64() {
        String authHeader = "Basic !@#$%^&*()";
        assertFalse(userService.isValidAuth(authHeader));
    }

    @Test
    public void testIsValidAuthWithMissingPrefix() {
        String authHeader = Base64.getEncoder().encodeToString("user1:password1".getBytes());
        assertFalse(userService.isValidAuth(authHeader));
    }

    @Test
    public void testIsValidAuthWithEmptyUsername() {
        String authHeader = encodeCredentials("", "password1");
        assertFalse(userService.isValidAuth(authHeader));
    }

    @Test
    public void testIsValidAuthWithEmptyPassword() {
        String authHeader = encodeCredentials("user1", "");
        assertFalse(userService.isValidAuth(authHeader));
    }

    @Test
    public void testIsValidAuthWithNoColon() {
        String authHeader = "Basic " + Base64.getEncoder().encodeToString("user1password1".getBytes());
        assertFalse(userService.isValidAuth(authHeader));
    }
}
