# Kafka-like Message Broker in Java

This is a simplified in-memory message broker system written in Java, inspired by Apache Kafka. It supports basic pub-sub (publish-subscribe) functionality, with the ability to:

- Create topics
- Register publishers and subscribers
- Publish messages to topics
- Notify subscribers in real-time
- Replay messages from the beginning using offset resets

## Features

- Thread-safe topic and subscriber management
- Message publishing with subscriber notification
- Late subscribers receive only future messages
- Subscribers can reset their offset to reprocess all past messages
- Multi-threaded message consumption using `ExecutorService`

## How It Works

### Topics
- Topics are created via the `KafkaManager.createTopic(String name)` method. Each topic has an ID and name.
- Each topic maintains its own list of messages. Messages are added via `Topic.addMessage(Message message)`.

### Publishers
- A publisher is responsible for publishing messages to topics. It implements the `IPublisher` interface, specifically the `publish(Topic topic, Message message)` method.
- Publishers can create messages and push them to any available topic.

### Subscribers
- A subscriber can subscribe to any existing topic via `KafkaManager.subscribe(String topicId, ISubscriber subscriber)`.
- Once subscribed, the subscriber consumes messages from the topic in its own thread using the `onMessage(Message message)` method. Subscribers are notified when new messages are available.
- The subscriber keeps track of the offset, which is the index of the last consumed message.

### Offset Reset
- A subscriber can reset its offset to re-consume messages from the beginning of the topic. This is done using the `KafkaManager.resetOffset(ISubscriber subscriber, String topicId)` method.
- After the reset, the subscriber processes all messages from the start, allowing it to "replay" the messages.

## Getting Started

### Prerequisites
- Java 8 or above is required to run this application.
- No external dependencies are needed.

### Running the Application

1. Compile the Java files:

```bash
javac main/java/*.java
