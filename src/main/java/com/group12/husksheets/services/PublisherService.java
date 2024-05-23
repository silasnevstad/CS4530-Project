package com.group12.husksheets.services;

import com.group12.husksheets.models.Publisher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class PublisherService {
    private final Map<String, Publisher> publishers = new HashMap<>();
    private final Map<String, List<String>> sheets = new HashMap<>();

    public boolean addPublisher(String name) {
        if (publishers.containsKey(name)) {
            return false;
        }
        publishers.put(name, new Publisher(name));
        sheets.put(name, new ArrayList<>());
        return true;
    }

    public Map<String, Publisher> getPublishers() {
        return publishers;
    }

    public boolean createSheet(String publisher, String sheet) {
        if (!publishers.containsKey(publisher) || sheets.get(publisher).contains(sheet)) {
            return false;
        }
        sheets.get(publisher).add(sheet);
        return true;
    }

    public boolean deleteSheet(String publisher, String sheet) {
        if (!publishers.containsKey(publisher) || !sheets.get(publisher).contains(sheet)) {
            return false;
        }
        sheets.get(publisher).remove(sheet);
        return true;
    }

    public List<String> getSheets(String publisher) {
        return sheets.get(publisher);
    }
}
