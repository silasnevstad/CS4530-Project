package com.group12.husksheets.ui.models;

import java.util.ArrayList;

public class Sheet {

  String name;
  ArrayList<User> usersWithAccess = new ArrayList<>();

  public Sheet(String name) {
    this.name = name;
  }

  public String name() {
    return this.name;
  }

  public boolean canUserAccess(User user) {

    for (User userWithAccess : usersWithAccess) {
      if (userWithAccess.sameUser(user)) {
        return true;
      }
    }

    return false;

  }

  public void giveUserAccess(User user) {
    usersWithAccess.add(user);
  }
}