package com.matejik.terminal.common.concurrent;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Provides access to the bounded shared executor for background work. All asynchronous flows
 * outside the Vaadin UI thread should use this pool to avoid unbounded thread creation.
 */
@Component
public class SharedBackendPool {

  private final ExecutorService executor;

  public SharedBackendPool(@Qualifier("sharedBackendExecutor") ExecutorService executor) {
    this.executor = Objects.requireNonNull(executor, "executor");
  }

  public ExecutorService executor() {
    return executor;
  }

  public CompletableFuture<Void> runAsync(Runnable runnable) {
    return CompletableFuture.runAsync(runnable, executor);
  }

  public <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier) {
    return CompletableFuture.supplyAsync(supplier, executor);
  }
}
