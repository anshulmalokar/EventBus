package models;

import Wrapper.EventId;
import Wrapper.SubscriberId;
import Wrapper.Topic;

import java.util.function.Function;

public class Subscription {
    private final EventId id;
    private final Topic topic;
    private final SubscriberId subscriberId;
    private final Type type;
    private final Function<Event, Boolean> precondition;

    public Subscription(EventId  id, Topic topic, SubscriberId  subscriberId, Type type, Function<Event, Boolean> precondition) {
        this.id = id;
        this.topic = topic;
        this.subscriberId = subscriberId;
        this.type = type;
        this.precondition = precondition;
    }
}
