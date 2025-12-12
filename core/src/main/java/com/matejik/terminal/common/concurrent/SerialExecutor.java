package com.matejik.terminal.common.concurrent;

import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Ensures submitted tasks are executed sequentially while delegating execution to a shared backend
 * executor.
 */
public final class SerialExecutor implements Executor {

  private final Executor backend;
  private final Consumer<Throwable> errorHandler;
  private final ConcurrentLinkedQueue<Runnable> queue = new ConcurrentLinkedQueue<>();
  private final AtomicBoolean draining = new AtomicBoolean(false);

  public SerialExecutor(Executor backend, Consumer<Throwable> errorHandler) {
    this.backend = Objects.requireNonNull(backend, "backend");
    this.errorHandler = Objects.requireNonNull(errorHandler, "errorHandler");
  }

  @Override
  public void execute(Runnable command) {
    Objects.requireNonNull(command, "command");
    queue.add(command);
    schedule();
  }

  public int queuedTaskCount() {
    return queue.size();
  }

  private void schedule() {
    if (draining.compareAndSet(false, true)) {
      try {
        backend.execute(this::drain);
      } catch (RejectedExecutionException executionException) {
        draining.set(false);
        queue.poll();
        throw executionException;
      }
    }
  }

  private void drain() {
    try {
      Runnable task;
      while ((task = queue.poll()) != null) {
        try {
          task.run();
        } catch (Throwable throwable) {
          errorHandler.accept(throwable);
        }
      }
    } finally {
      draining.set(false);
      if (!queue.isEmpty()) {
        schedule();
      }
    }
  }
}
