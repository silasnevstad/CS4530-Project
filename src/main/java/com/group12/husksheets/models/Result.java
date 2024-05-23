package com.group12.husksheets.models;

import java.util.List;

public class Result {
    public boolean success;
    public String message;
    public List<Argument> value;

    public Result(boolean success, String message, List<Argument> value) {
        this.success = success;
        this.message = message;
        this.value = value;
    }
}
