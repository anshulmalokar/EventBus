package utils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public  class KeyedExecutor<T> {
    private Executor[] executors;

    public KeyedExecutor(int size) {
        executors = new Executor[size];
        for (int i = 0; i < size; i++) {
            executors[i] = Executors.newSingleThreadExecutor();
        }
    }

    public CompletionStage<Void> submit(String topic, Runnable r) {
        int hash = topic.hashCode();
        int idx = hash % executors.length;
        return CompletableFuture.runAsync(r, executors[idx]);
    }

    public CompletionStage<T> submit(String topic, Supplier<T> r) {
        int hash = topic.hashCode();
        int idx = hash % executors.length;
        return CompletableFuture.supplyAsync(r, executors[idx]);
    }

}
