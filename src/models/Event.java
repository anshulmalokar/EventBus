package models;

import java.util.Map;

public class Event {
    private final String id;
    private final String topic;
    private final Map<String, Object> attributes;

    public Event(String id, String topic, Map<String, Object> attributes) {
        this.id = id;
        this.topic = topic;
        this.attributes = attributes;
    }
}
