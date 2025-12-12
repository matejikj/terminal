package com.matejik.terminal.application.store;

import com.matejik.terminal.common.concurrent.SerialExecutor;
import java.util.function.Consumer;

public interface SerialExecutorFactory {

  SerialExecutor create(Consumer<Throwable> errorHandler);
}
