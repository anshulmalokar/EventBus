package models;

import Wrapper.EventId;

import java.util.UUID;

public class FaliureEvent extends Event{
    private Event event;
    private Throwable t;
    private long timeStamp;

    public FaliureEvent(Event event, Throwable t, long timeStamp) {
        super(new EventId(UUID.randomUUID().toString()), "faliure "+ event.getName() ,event.getTopic(), timeStamp, event.getAttributes());
        this.event = event;
        this.t = t;
        this.timeStamp = timeStamp;
    }
}
