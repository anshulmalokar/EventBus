import Exceptions.EventNotFoundException;
import Wrapper.EventId;
import Wrapper.SubscriberId;
import Wrapper.Topic;
import models.Event;
import models.Subscription;
import utils.KeyedExecutor;

import java.util.*;
import java.util.concurrent.*;

class EventBus{
    private final Map<Topic, Map<EventId, Event>> buses;
    private final Map<Topic, Set<Subscription>> subscribers;
    private final Map<Topic, Map<SubscriberId, EventId>> subscriberIndex;
    private final Map<Topic, ConcurrentSkipListMap<Long, EventId>> eventTimeStamp;
    private KeyedExecutor executor;

    public EventBus(){
        executor = new KeyedExecutor(10);
        this.buses = new ConcurrentHashMap<>();
        this.subscribers = new ConcurrentHashMap<>();
        this.subscriberIndex = new ConcurrentHashMap<>();
        this.eventTimeStamp = new ConcurrentHashMap<>();
    }

    public CompletionStage<Event> poll(Topic topic, SubscriberId subscriberId){
        return executor.submit(topic.getTopic()+ subscriberId.getSubscriberId() ,() -> {
            EventId index = subscriberIndex.get(topic).get(subscriberId);
            Event e = buses.get(topic).get(index);
            return e;
        });
    }

    public void publishEvent(Topic topic, Event event){
        executor.submit(topic.getTopic(), () -> addEvent(topic, event));
    }

    public void push(Event event, SubscriberId subscriberId){

    }

    private void addEvent(Topic topic, Event event){
        buses.putIfAbsent(topic, new ConcurrentHashMap<>());
        eventTimeStamp.putIfAbsent(topic, new ConcurrentSkipListMap<>());
        eventTimeStamp.get(topic).put(event.getTimeStamp(), event.getId());
    }

    public void subscribeForPull(Topic topic, Subscription subscription){
        subscribers.putIfAbsent(topic, new CopyOnWriteArraySet<>());
        subscribers.get(topic).add(subscription );
    }

    public void setIndexFromTimeStamp(Topic topic, SubscriberId subscriberId, long timestamp){
        final EventId eventId = eventTimeStamp.get(topic).higherEntry(timestamp).getValue();
        subscriberIndex.putIfAbsent(topic ,new ConcurrentHashMap<>());
        subscriberIndex.get(topic).put(subscriberId, eventId);
    }

    public void setIndexFromAnEvent(Topic topic, SubscriberId subscriberId, EventId eventId){
        subscriberIndex.putIfAbsent(topic ,new ConcurrentHashMap<>());
        subscriberIndex.get(topic).put(subscriberId, eventId);
    }

    public void pullEvent(){

    }

    public Event getEvent(String topic, String eventId){
        return buses.get(topic).get(eventId);
    }

}


