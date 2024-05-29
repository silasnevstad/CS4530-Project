package com.group12.husksheets.services;

import java.util.Base64;
import java.util.HashMap;

public class UserService {
    private final HashMap<String, String> users = new HashMap<>();

    public UserService() {
        users.put("user1", "password1");
        users.put("user2", "password2");
    }

    public boolean isValidUser(String authHeader) {
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

    private boolean checkCredentials(String username, String password) {
        return users.containsKey(username) && users.get(username).equals(password);
    }
}
