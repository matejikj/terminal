package com.matejik.terminal.application.store;

import com.matejik.terminal.common.concurrent.SerialExecutor;
import java.util.function.Consumer;

public interface SerialActionDispatcher {

  SerialExecutor create(Consumer<Throwable> errorHandler);
}
