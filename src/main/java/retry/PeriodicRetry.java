package main.java.retry;

public class PeriodicRetry<T, R> extends RetryAlgorithm<T, R> {

    public PeriodicRetry(int maxAttempts, long waitTime) {
        super(maxAttempts, waitTime);
    }

    @Override
    long getSleepTime(int attempts) {
        return attempts * super.getWaitTime();
    }
}
