package com.group12.husksheets.services;

import com.group12.husksheets.models.Argument;
import com.group12.husksheets.models.Publisher;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class PublisherService {
    private final HashMap<String, Publisher> publishers = new HashMap<>();
    private final HashMap<String, List<String>> sheets = new HashMap<>();
    private final HashMap<String, HashMap<String, List<Argument>>> updates = new HashMap<>();
    private final HashMap<String, HashMap<String, List<Argument>>> subscriptions = new HashMap<>();

    public boolean addPublisher(String name) {
        if (publishers.putIfAbsent(name, new Publisher(name)) != null) {
            return false;
        }
        sheets.put(name, new ArrayList<>());
        updates.put(name, new HashMap<>());
        subscriptions.put(name, new HashMap<>());
        return true;
    }

    public HashMap<String, Publisher> getPublishers() {
        return publishers;
    }

    public boolean createSheet(String publisher, String sheet) {
        if (!publishers.containsKey(publisher) || sheets.get(publisher).contains(sheet)) {
            return false;
        }
        sheets.get(publisher).add(sheet);
        updates.get(publisher).put(sheet, new ArrayList<>());
        subscriptions.get(publisher).put(sheet, new ArrayList<>());
        return true;
    }

    public boolean deleteSheet(String publisher, String sheet) {
        if (!publishers.containsKey(publisher) || !sheets.get(publisher).contains(sheet)) {
            return false;
        }
        sheets.get(publisher).remove(sheet);
        updates.get(publisher).remove(sheet);
        subscriptions.get(publisher).remove(sheet);
        return true;
    }

    public List<String> getSheets(String publisher) {
        return sheets.get(publisher);
    }

    public List<Argument> getUpdatesForSubscription(String publisher, String sheet, String id) {
        return getUpdates(subscriptions, publisher, sheet, id);
    }

    public List<Argument> getUpdatesForPublished(String publisher, String sheet, String id) {
        return getUpdates(updates, publisher, sheet, id);
    }

    private List<Argument> getUpdates(HashMap<String, HashMap<String, List<Argument>>> map, String publisher, String sheet, String id) {
        if (!map.containsKey(publisher) || !map.get(publisher).containsKey(sheet)) {
            return new ArrayList<>();
        }
        List<Argument> updatesList = map.get(publisher).get(sheet);
        List<Argument> result = new ArrayList<>();
        boolean startCollecting = id == null || id.isEmpty();
        for (Argument update : updatesList) {
            if (startCollecting) {
                result.add(update);
            }
            if (update.id.equals(id)) {
                startCollecting = true;
            }
        }
        return result;
    }

    public boolean updatePublished(String publisher, String sheet, String payload) {
        return update(updates, publisher, sheet, payload);
    }

    public boolean updateSubscription(String publisher, String sheet, String payload) {
        return update(subscriptions, publisher, sheet, payload);
    }

    private boolean update(HashMap<String, HashMap<String, List<Argument>>> map, String publisher, String sheet, String payload) {
        if (!map.containsKey(publisher) || !map.get(publisher).containsKey(sheet)) {
            return false;
        }
        Argument argument = new Argument();
        argument.publisher = publisher;
        argument.sheet = sheet;
        argument.payload = payload;
        argument.id = uuid();
        map.get(publisher).get(sheet).add(argument);
        return true;
    }

    private String uuid() {
        return java.util.UUID.randomUUID().toString();
    }
}
