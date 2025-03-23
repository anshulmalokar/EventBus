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

    public R attempt(Function<T, R> f, T paramater, int attempts) throws RertyLimitReachedException {
        try {
            return f.apply(paramater);
        } catch (Exception e) {
            if (e instanceof RetryableException) {
                if (attempts == maxAttempts) {
                    throw new RertyLimitReachedException("Limit reached");
                }
                R r = attempt(f, paramater, attempts + 1);;
                long sleepTime = getSleepTime(attempts);
                try {
                    Thread.sleep(sleepTime);
                    return r;
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
            throw new RuntimeException();
        }
    }

    public long getWaitTime() {
        return this.waitTime;
    }

    abstract long getSleepTime(int attempts);
}
