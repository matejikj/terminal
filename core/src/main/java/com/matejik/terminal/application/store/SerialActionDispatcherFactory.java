package com.matejik.terminal.application.store;

import com.matejik.terminal.common.concurrent.SerialExecutor;
import com.matejik.terminal.common.concurrent.SharedBackendPool;
import java.util.function.Consumer;
import org.springframework.stereotype.Component;

@Component
final class SerialActionDispatcherFactory implements SerialActionDispatcher {

  private final SharedBackendPool backendPool;

  SerialActionDispatcherFactory(SharedBackendPool backendPool) {
    this.backendPool = backendPool;
  }

  @Override
  public SerialExecutor create(Consumer<Throwable> errorHandler) {
    return new SerialExecutor(backendPool.executor(), errorHandler);
  }
}
