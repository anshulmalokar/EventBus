package main.java.models;

import main.java.Exceptions.RertyLimitReachedException;
import main.java.Wrapper.EntityId;
import main.java.Wrapper.EventId;
import main.java.Wrapper.Topic;
import main.java.retry.RetryAlgorithm;
import main.java.utils.KeyedExecutor;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class EventBus {
    private final Map<Topic, Map<EventId, Event>> buses;
    private final Map<Topic, Set<Subscription>> subscribers;
    private final Map<Topic, Map<EntityId, EventId>> subscriberIndex;
    private final Map<Topic, ConcurrentSkipListMap<Long, EventId>> eventTimeStamp;
    private final RetryAlgorithm retryAlgorithm;
    private final KeyedExecutor executor;
    private final EventBus deadLetterQueue;
    private final main.java.utils.Timer timer;

    public EventBus(RetryAlgorithm retryAlgorithm, EventBus deadLetterQueue) {
        this.retryAlgorithm = retryAlgorithm;
        this.deadLetterQueue = deadLetterQueue;
        executor = new KeyedExecutor(10);
        this.buses = new ConcurrentHashMap<>();
        this.subscribers = new ConcurrentHashMap<>();
        this.subscriberIndex = new ConcurrentHashMap<>();
        this.eventTimeStamp = new ConcurrentHashMap<>();
        timer = main.java.utils.Timer.getInstance();
    }

    public CompletionStage<Event> poll(Topic topic, EntityId subscriberId) {
        return executor.submit(topic.getValue() + subscriberId.getEntityId(), () -> {
            EventId index = subscriberIndex.get(topic).get(subscriberId);
            Event e = buses.get(topic).get(index);
            return e;
        });
    }

    public void publishEvent(Topic topic, Event event) {
        executor.submit(topic.getValue(), () -> addEventToBus(topic, event));
    }

    public void push(Event event, Subscription subscription) {
        executor.submit(event.getTopic().getValue() + subscription.getId(), () -> {
            try {
                retryAlgorithm.attempt(subscription.handler(), event, 10);
            } catch (RertyLimitReachedException e) {
                if (deadLetterQueue != null) {
                    deadLetterQueue.publishEvent(event.getTopic(),
                            new FaliureEvent(event, e, timer.getTime()));
                } else {
                    e.printStackTrace();
                }
            }
        });
    }

    private void addEventToBus(Topic topic, Event event) {
        buses.putIfAbsent(topic, new ConcurrentHashMap<>());
        eventTimeStamp.putIfAbsent(topic, new ConcurrentSkipListMap<>());
        buses.get(topic).put(event.getId(), event);
        eventTimeStamp.get(topic).put(event.getTimeStamp(), event.getId());
        subscribers.getOrDefault(topic, new CopyOnWriteArraySet<>()).stream()
                .filter(s -> Type.PUSH.equals(s.getType()))
                .forEach(s -> push(event, s));
    }

    public void subscribeForPull(Topic topic, Subscription subscription) {
        subscribers.putIfAbsent(topic, new CopyOnWriteArraySet<>());
        subscribers.get(topic).add(subscription);
    }

    public void setIndexFromTimeStamp(Topic topic, EntityId subscriberId, long timestamp) {
        final EventId eventId = eventTimeStamp.get(topic).higherEntry(timestamp).getValue();
        subscriberIndex.putIfAbsent(topic, new ConcurrentHashMap<>());
        subscriberIndex.get(topic).put(subscriberId, eventId);
    }

    public void setIndexFromEvent(Topic topic, EntityId subscriberId, EventId eventId) {
        subscriberIndex.putIfAbsent(topic, new ConcurrentHashMap<>());
        subscriberIndex.get(topic).put(subscriberId, eventId);
    }

    public void pullEvent() {

    }

    public Event getEvent(String topic, String eventId) {
        return buses.get(topic).get(eventId);
    }

}
