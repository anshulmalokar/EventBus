package utils;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Timer {
    private static Timer instance = null;
    private static final Lock mtx = new ReentrantLock();

    private Timer() {
    }

    ;

    public static Timer getInstance() {
        try {
            if (instance == null) {
                mtx.lock();
                if (instance == null) {
                    return instance = new Timer();
                }
            }
        } finally {
            mtx.unlock();
        }
        return instance;
    }


    public long getTime() {
        return System.nanoTime();
    }
}
