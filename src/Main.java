import models.Event;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

class EventBus{
    private final Map<String, List<Event>> topics;
    public EventBus(){
        this.topics = new ConcurrentHashMap<>();
    }
}

