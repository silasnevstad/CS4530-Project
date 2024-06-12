package com.group12.husksheets.models;

import java.util.ArrayList;

public class User {

  String username;
  String password;


  public User(String username, String password) {
    this.username = username;
    this.password = password;
  }

  public boolean sameUser(User otherUser) {
    return otherUser.isUsername(this.username) && otherUser.isPassword(this.password);
  }

  public boolean isUsername(String username) {
    return this.username.equals(username);
  }

  public boolean isPassword(String password) {
    return this.password.equals(password);
  }

  public ArrayList<Sheet> accessibleSheets(ArrayList<Sheet> allSheets) {
    ArrayList<Sheet> result = new ArrayList<>();

    for(Sheet sheet : allSheets) {
      if(sheet.canUserAccess(this)) {
        result.add(sheet);
      }
    }

    return result;
  }

}