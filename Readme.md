# EventBus Java Project

This is a Java-based EventBus system that satisfies the following requirements:

## Features

1. **Multiple Publishers and Subscribers**:  
   Supports registration from multiple classes to the EventBus.

2. **Causal Ordering of Topics**:  
   Ensures the correct sequence of events for each topic.

3. **Configurable Retry Attempts**:  
   Supports retry logic for failed event delivery.

4. **Dead Letter Queue (DLQ)**:  
   Events that cannot be delivered after retry attempts are sent to a dead letter queue.

5. **Idempotency on Event Receiving**:  
   Ensures that event processing is idempotent (the same event is not processed multiple times).

6. **Pull and Push Models**:  
   Supports both pull-based and push-based models for event delivery.

7. **Subscribe from Timestamp or Offset**:  
   Subscribers can choose to start receiving events from a specific timestamp or offset.

8. **Preconditions for Event Subscription**:  
   Subscribers can define preconditions that must be met before receiving events.

## How to Set Up

### 1. Clone the Repository

Clone this repository to your local machine:

```bash
git clone https://github.com/your-repository-url/eventbus-java.git

