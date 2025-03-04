import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.function.Supplier;

class Event{
    private String id;
    private String name;
    private Map<String, Object> attributes;
    public Event(String id, String name, Map<String, Object> attributes){
        this.id = id;
        this.name = name;
        this.attributes = attributes;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}

class EventBus{
    private final Map<String, List<Event>> topicMap;
    private final Map<String, Set<Subscription>> subscriber;
    private final KeyedExecutor executor;

    public EventBus(){
        topicMap = new ConcurrentHashMap<>();
        executor = new KeyedExecutor(10);
        subscriber = new ConcurrentHashMap<>();
    }
    public void publish(String topic, Event event){
        if(!topicMap.containsKey(topic)){
            topicMap.put(topic,new ArrayList<>());
        }
        executor.submit(topic, () -> topicMap.get(topic).add(event));
    }

    public void subscribeToPullStrategy(String topic, Subscription subscription){
        if(!this.subscriber.containsKey(topic)){
            this.subscriber.put(topic, new HashSet<>());
        }
        subscriber.get(topic).add(subscription);
    }

    public Event getEvent(String topic, String eventId){
        return topicMap.get(topic).stream()
                .filter((e) -> e.getId().equals(eventId))
                .findAny()
                .orElseThrow(EventNotFoundException::new);
    }

    public void getEventsByTimeStamp(){

    }
}

class Subscription<T>{
    private int id;
    private String subscriberId;
    private String topic;
    private SubscriptionType type;
    private Function<T, Boolean> precondition;
    public Subscription(int id, String subscriberId, String topic, Function<T, Boolean> precondition, SubscriptionType type) {
        this.id = id;
        this.subscriberId = subscriberId;
        this.topic = topic;
        this.precondition = precondition;
        this.type = type;
    }
}

enum SubscriptionType{
    PULL,
    PUSH
}

abstract class RetryAlgorithm<P, R> {
    public abstract void retry(Function<P,R> task, P paramater, int attempts) throws RetryLimitExceedException;
}

class PeriodicRetry<P,R> extends RetryAlgorithm<P,R>{
    private final int maxAttempts;
    private final long maxInterval;

    PeriodicRetry(int maxAttempts, long maxInterval) {
        this.maxAttempts = maxAttempts;
        this.maxInterval = maxInterval;
    }

    @Override
    public void retry(Function<P, R> task, P paramater, int attempts) throws RetryLimitExceedException {
        int attempt = 1;
        while (attempt <= maxAttempts){
            try {
                task.apply(paramater);
                return;
            }catch (Exception e){
                if (e instanceof RetryAbleException){
                   if(attempts == maxAttempts){
                       throw new RetryLimitExceedException();
                   }else{
                       try {
                           Thread.sleep(maxInterval);
                       } catch (InterruptedException ex) {
                           throw new RuntimeException(ex);
                       }
                   }
                }else{
                    throw new RuntimeException(e);
                }
            }
            attempt++;
        }
    }
}

class ExponentialBackOff<P,R> extends RetryAlgorithm<P,R>{
    private final Timer timer;
    private int maxAttempts;
    public ExponentialBackOff(int maxAttempts) {
        this.timer = Timer.getInstance();
        this.maxAttempts = maxAttempts;
    }

    @Override
    public void retry(Function<P,R> task, P paramater, int attempts) throws RetryLimitExceedException {
        try{
            task.apply(paramater);
        }catch (Exception e){
            attempts++;
            if(e instanceof RetryAbleException){
                if(attempts == maxAttempts){
                    throw new RetryLimitExceedException();
                }else{
                    long timeToSleep = (long) (Math.pow(attempts-1, 2) * 1000);
                    try {
                        Thread.sleep(timeToSleep);
                        retry(task, paramater, attempts+1);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }else{
                throw new RuntimeException(e);
            }
        }
    }
}

class RetryLimitExceedException extends RuntimeException{ }

class RetryAbleException extends RuntimeException{ }

class EventNotFoundException extends RuntimeException{ }

class Timer{
    private static Timer instance;
    private final static Lock mtx = new ReentrantLock();
    private Timer(){};
    public static Timer getInstance(){
        if(instance == null){
            try {
                mtx.lock();
                if(instance == null){
                    instance = new Timer();
                    return instance;
                }
            }finally {
                mtx.unlock();
            }
        }
        return instance;
    }
    public long getTime(){
        return System.nanoTime();
    }
}

class KeyedExecutor<T>{
    private Executor[] executor;

    public KeyedExecutor(int size){
        executor = new Executor[size];
        for(int i=0;i<size;i++){
            executor[i] = Executors.newSingleThreadExecutor();
        }
    }

    public CompletionStage<Void> submit(String topic , Runnable r){
        return CompletableFuture.runAsync(r, executor[topic.hashCode()%executor.length]);
    }

    public CompletionStage<T> get(String topic , Supplier<T> r){
        return CompletableFuture.supplyAsync(r, executor[topic.hashCode()%executor.length]);
    }
}

public class Main {
    public static void main(String[] args) {

    }
}