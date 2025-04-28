package main.java;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Main{
    public static void main(String[] args) throws InterruptedException {
        KafkaManager kafkaManager = new KafkaManager();

        // Test 1: Create Topics
        Topic t1 = kafkaManager.createTopic("Topic-1");
        Topic t2 = kafkaManager.createTopic("Topic-2");

        // Test 2: Create Publisher and Subscribers
        IPublisher publisher = new Publisher("publisher-1");
        ISubscriber subscriber1 = new Subscriber("subscriber-1");
        ISubscriber subscriber2 = new Subscriber("subscriber-2");
        ISubscriber subscriber3 = new Subscriber("subscriber-3");

        // Test 3: Subscribe subscriber1 and subscriber2 to Topic-1
        kafkaManager.subscribe(t1.id, subscriber1);
        kafkaManager.subscribe(t1.id, subscriber2);

        // Test 4: Publish messages to Topic-1
        kafkaManager.publish(t1.id, publisher, new Message("Topic-1 message-1"));
        kafkaManager.publish(t1.id, publisher, new Message("Topic-1 message-2"));

        // Let subscribers process messages
        Thread.sleep(1000);

        // Test 5: Subscribe subscriber3 after some messages were already published
        kafkaManager.subscribe(t1.id, subscriber3);
        kafkaManager.publish(t1.id, publisher, new Message("Topic-1 message-3"));

        // Let all subscribers process messages
        Thread.sleep(1000);

        // Test 6: Publish to Topic-2 without any subscribers
        kafkaManager.publish(t2.id, publisher, new Message("Topic-2 message-1"));

        // Let system process
        Thread.sleep(500);

        // Test 7: Subscribe to non-existent topic
        kafkaManager.subscribe("invalid-topic-id", new Subscriber("subscriber-4"));

        // Test 8: Publish to non-existent topic
        kafkaManager.publish("invalid-topic-id", publisher, new Message("bad message"));


        // Test 9: Reset Offset and Replay Messages
        ISubscriber resetSubscriber = new Subscriber("reset-subscriber");
        kafkaManager.subscribe(t1.id, resetSubscriber);
        kafkaManager.publish(t1.id, publisher, new Message("Topic-1 message-A"));
        kafkaManager.publish(t1.id, publisher, new Message("Topic-1 message-B"));
        kafkaManager.publish(t1.id, publisher, new Message("Topic-1 message-C"));

        Thread.sleep(1000); // allow processing

        System.out.println("Resetting offset for reset-subscriber");
        kafkaManager.resetOffset(resetSubscriber, t1.id);

        Thread.sleep(1000); // allow reprocessing

        kafkaManager.shutdown();
    }
}
class TopicSubscriberRunableTask implements Runnable{
    public final TopicSubscriber topicSubscriber;
    public TopicSubscriberRunableTask(TopicSubscriber topicSubscribers){
        this.topicSubscriber = topicSubscribers;
    }

    @Override
    public void run() {
        while (true){
            Message currentMessageToProcess = null;
            synchronized (topicSubscriber){
                while (this.topicSubscriber.offset.get() >= topicSubscriber.topic.getMessages().size()){
                    try {
                        topicSubscriber.wait();
                    }catch (InterruptedException e){
                        Thread.currentThread().interrupt();;
                    }
                }
                if(!topicSubscriber.isActive){
                    return;
                }
                int currentIndex = this.topicSubscriber.offset.getAndIncrement();
                Message message = this.topicSubscriber.topic.getMessages().get(currentIndex);
                currentMessageToProcess = message;
            }
            if(currentMessageToProcess != null){
                this.topicSubscriber.subscriber.onMessage(currentMessageToProcess);
            }
        }
    }
}
class KafkaManager{
    private final Map<String, Topic> topics;
    private final Map<String, List<TopicSubscriber>> topicToSubscribers;
    private final ExecutorService executorService;
    private final AtomicInteger idGenerator;
    public KafkaManager(){
        this.topics = new ConcurrentHashMap<>();
        this.topicToSubscribers = new ConcurrentHashMap<>();
        executorService = Executors.newCachedThreadPool();
        this.idGenerator = new AtomicInteger(1);
    }

    public Topic createTopic(String name){
        Topic topic = new Topic();
        topic.id = this.idGenerator.incrementAndGet() + "";
        topic.name = name;
        topics.put(topic.id, topic);
        topicToSubscribers.put(topic.id, new ArrayList<>());
        return topic;
    }

    public void publish(String topicId, IPublisher publisher,Message message){
        Topic topic = topics.get(topicId);
        if(topic == null){
            System.out.println("The ");
            return;
        }
        publisher.publish(topic, message);
        List<TopicSubscriber> topicSubscribers = topicToSubscribers.get(topicId);
        for(TopicSubscriber ts: topicSubscribers){
            synchronized (ts){
                ts.notify();
            }
        }
        System.out.println("Publisher with id :" + publisher.getId() + " have published the message " +
                " " + message.text + " " + "also the notification for all have been sent");
    }

    public void subscribe(String topicId, ISubscriber subscriber){
        Topic topic = topics.get(topicId);
        if(topic == null){
            System.out.println("The ");
            return;
        }
        TopicSubscriber ts = new TopicSubscriber(subscriber, topic);
        topicToSubscribers.get(topicId).add(ts);
        executorService.submit(new TopicSubscriberRunableTask(ts));
        System.out.println("Subscriber with id " + subscriber.getId() + " have subscribed to the topic " + " " +
                topic.name);
    }

    public void shutdown(){
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }

    public void resetOffset(ISubscriber subscriber, String topicId){
        Topic topic = topics.get(topicId);
        if(topic == null){
            System.out.println("The ");
            return;
        }
        List<TopicSubscriber> li = this.topicToSubscribers.get(topicId);
        for(TopicSubscriber sub: li){
            if(sub.subscriber.equals(subscriber)){
                sub.offset = new AtomicInteger(0);
                synchronized (sub){
                    sub.notify();
                }
                break;
            }
        }
    }
}
class Message{
    public String text;

    public Message(String text) {
        this.text = text;
    }
}
class Topic{
    public String id;
    public String name;
    private List<Message> messages;

    public Topic(){
        this.messages = new ArrayList<>();
    }

    public synchronized void addMessage(Message message){
        this.messages.add(message);
    }

    public synchronized List<Message> getMessages() {
        return Collections.unmodifiableList(messages);
    }
}
interface IPublisher{
    void publish(Topic topic, Message message);
    String getId();
}
class Publisher implements IPublisher{
    private final String id;
    public Publisher(String id){
        this.id = id;
    }
    @Override
    public void publish(Topic topic, Message message) {
        topic.addMessage(message);
    }

    @Override
    public String getId() {
        return id;
    }
}
interface ISubscriber{
    void onMessage(Message message);
    String getId();
}
class Subscriber implements ISubscriber{
    private final String id;
    public Subscriber(String id){
        this.id = id;
    }

    @Override
    public void onMessage(Message message) {
        System.out.println("Subscriber with the id :" + id + " got a message " + message.text);
    }

    @Override
    public String getId() {
        return id;
    }
}
class TopicSubscriber{
    public final ISubscriber subscriber;
    public final Topic topic;
    public AtomicInteger offset;
    public boolean isActive;
    public TopicSubscriber(ISubscriber subscriber, Topic topic){
        this.subscriber = subscriber;
        this.topic = topic;
        this.offset = new AtomicInteger(0);
        this.isActive = true;
    }
}