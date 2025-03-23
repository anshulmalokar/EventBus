package main.java.retry;

import main.java.utils.Timer;

public class ExponentialBackOff<T, R> extends RetryAlgorithm<T, R> {

    public ExponentialBackOff(int maxAttempts) {
        super(maxAttempts, 0);
    }

    @Override
    long getSleepTime(int attempts) {
        return (long) Math.pow(2, attempts) * Timer.getInstance().getTime();
    }
}
