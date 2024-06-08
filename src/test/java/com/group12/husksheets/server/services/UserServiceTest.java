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
        assertTrue(userService.isValidUser(authHeader));
    }

    @Test
    public void testIsValidUserWithInvalidCredentials() {
        String authHeader = encodeCredentials("user1", "wrongpassword");
        assertFalse(userService.isValidUser(authHeader));
    }

    @Test
    public void testIsValidUserWithNonExistentUser() {
        String authHeader = encodeCredentials("nonexistent", "password");
        assertFalse(userService.isValidUser(authHeader));
    }

    @Test
    public void testIsValidUserWithMalformedHeader() {
        String authHeader = "Basic malformed";
        assertFalse(userService.isValidUser(authHeader));
    }

    @Test
    public void testIsValidUserWithEmptyHeader() {
        String authHeader = "";
        assertFalse(userService.isValidUser(authHeader));
    }

    @Test
    public void testIsValidUserWithNullHeader() {
        assertFalse(userService.isValidUser(null));
    }

    @Test
    public void testIsValidUserWithInvalidBase64() {
        String authHeader = "Basic !@#$%^&*()";
        assertFalse(userService.isValidUser(authHeader));
    }

    @Test
    public void testIsValidUserWithMissingPrefix() {
        String authHeader = Base64.getEncoder().encodeToString("user1:password1".getBytes());
        assertFalse(userService.isValidUser(authHeader));
    }

    @Test
    public void testIsValidUserWithEmptyUsername() {
        String authHeader = encodeCredentials("", "password1");
        assertFalse(userService.isValidUser(authHeader));
    }

    @Test
    public void testIsValidUserWithEmptyPassword() {
        String authHeader = encodeCredentials("user1", "");
        assertFalse(userService.isValidUser(authHeader));
    }

    @Test
    public void testIsValidUserWithNoColon() {
        String authHeader = "Basic " + Base64.getEncoder().encodeToString("user1password1".getBytes());
        assertFalse(userService.isValidUser(authHeader));
    }
}
