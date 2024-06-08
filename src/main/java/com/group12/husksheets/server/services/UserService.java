// Owner: Silas Nevstad

package com.group12.husksheets.server.services;

import java.util.Base64;
import java.util.HashMap;

public class UserService {
    private final HashMap<String, String> users = new HashMap<>();

    /**
     * Constructor for UserService.
     * Initializes the users with predefined usernames and passwords.
     */
    public UserService() {
        users.put("user1", "password1");
        users.put("user2", "password2");
    }

    /**
     * Validates if a user is authenticated based on the authorization header.
     *
     * @param authHeader The authorization header containing the Base64 encoded credentials.
     * @return true if the user is valid, false otherwise.
     */
    public boolean isValidUser(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            return false;
        }
        try {
            String decodedAuth = new String(
                    Base64.getDecoder().decode(authHeader.replace("Basic ", ""))
            );
            String[] creds = decodedAuth.split(":");
            if (creds.length != 2) {
                return false;
            }
            String username = creds[0];
            String password = creds[1];
            return checkCredentials(username, password);
        } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    /**
     * Checks if the provided username and password match the stored credentials.
     *
     * @param username The username to check.
     * @param password The password to check.
     * @return true if the credentials are valid, false otherwise.
     */
    private boolean checkCredentials(String username, String password) {
        return users.containsKey(username) && users.get(username).equals(password);
    }
}
