package com.group12.husksheets.models;

public class Argument {
    public String publisher;
    public String sheet;
    public String id;
    public String payload;

    public Argument() {
    }

    public Argument(String publisher, String sheet, String id, String payload) {
        this.publisher = publisher;
        this.sheet = sheet;
        this.id = id;
        this.payload = payload;
    }
}
