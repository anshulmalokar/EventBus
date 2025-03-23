package main.java.models;

import main.java.Wrapper.EventId;
import main.java.Wrapper.EntityId;
import main.java.Wrapper.Topic;

import java.util.function.Function;

public class Subscription {
    private final EventId id;
    private final Topic topic;
    private final EntityId subscriberId;
    private final Type type;
    private final Function<Event, Boolean> precondition;
    private final Function<Event, Void> handler;

    public EventId getId() {
        return id;
    }

    public Topic getTopic() {
        return topic;
    }

    public EntityId getSubscriberId() {
        return subscriberId;
    }

    public Type getType() {
        return type;
    }

    public Function<Event, Boolean> getPrecondition() {
        return precondition;
    }

    public Function<Event, Void> getHandler() {
        return handler;
    }

    public Subscription(EventId  id, Topic topic, EntityId subscriberId, Type type, Function<Event, Boolean> precondition, Function<Event, Void> handler) {
        this.id = id;
        this.topic = topic;
        this.subscriberId = subscriberId;
        this.type = type;
        this.precondition = precondition;
        this.handler = handler;
    }

    public Function<Event, Void> handler(){
        return handler;
    }
}
