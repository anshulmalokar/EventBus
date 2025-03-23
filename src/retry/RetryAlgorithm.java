package retry;

import Exceptions.RertyLimitReachedException;
import Exceptions.RetryableException;

import java.util.function.Function;

public abstract class RetryAlgorithm<T, R> {
    private final int maxAttempts;
    private final long waitTime;

    public RetryAlgorithm(int maxAttempts, long waitTime) {
        this.maxAttempts = maxAttempts;
        this.waitTime = waitTime;
    }

    public void retry(Function<T, R> f, T paramater, int attempts) throws RertyLimitReachedException {
        try {
            f.apply(paramater);
        } catch (Exception e) {
            if (e instanceof RetryableException) {
                if (attempts == maxAttempts) {
                    throw new RertyLimitReachedException("Limit reached");
                }
                long sleepTime = getSleepTime(attempts);
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
                retry(f, paramater, attempts + 1);
            }
            throw new RuntimeException();
        }
    }

    public long getWaitTime() {
        return this.waitTime;
    }

    abstract long getSleepTime(int attempts);
}
