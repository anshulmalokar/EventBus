package models;

import Wrapper.EventId;
import Wrapper.Topic;

import java.util.Map;

public class  Event {
    private final EventId id;
    private final String name;
    private final Topic topic;
    private final long timeStamp;
    private final Map<String, Object> attributes;

    public Event(EventId id, String name, Topic topic, long timeStamp, Map<String, Object> attributes) {
        this.id = id;
        this.name = name;
        this.topic = topic;
        this.timeStamp = timeStamp;
        this.attributes = attributes;
    }

    public String getName() {
        return name;
    }

    public Topic getTopic() {
        return topic;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public EventId getId(){
        return this.id;
    }
}
