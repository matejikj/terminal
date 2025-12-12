package com.matejik.terminal.application.store;

import com.matejik.terminal.application.state.AppState;
import com.matejik.terminal.application.state.actions.AppAction;
import com.matejik.terminal.application.state.reducer.AppStateReducer;
import com.matejik.terminal.common.concurrent.SerialExecutor;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@VaadinSessionScope
public class AppStore {

  private static final Logger LOGGER = LoggerFactory.getLogger(AppStore.class);

  private final AtomicReference<AppState> snapshot = new AtomicReference<>(AppState.initial());
  private final CopyOnWriteArrayList<Consumer<AppState>> listeners = new CopyOnWriteArrayList<>();
  private final SerialExecutor dispatcher;

  public AppStore(SerialExecutorFactory executorFactory) {
    this.dispatcher = executorFactory.create(this::handleDispatchFailure);
  }

  public AppState snapshot() {
    return snapshot.get();
  }

  public Registration subscribe(Consumer<AppState> listener) {
    Objects.requireNonNull(listener, "listener");
    listeners.add(listener);
    listener.accept(snapshot());
    return () -> listeners.remove(listener);
  }

  public CompletableFuture<AppState> dispatch(AppAction action) {
    Objects.requireNonNull(action, "action");
    var completion = new CompletableFuture<AppState>();
    dispatcher.execute(
        () -> {
          try {
            var previous = snapshot.get();
            var next = AppStateReducer.reduce(previous, action);
            if (!next.equals(previous)) {
              snapshot.set(next);
              notifyListeners(next);
            }
            completion.complete(snapshot.get());
          } catch (Throwable error) {
            completion.completeExceptionally(error);
            throw error;
          }
        });
    return completion;
  }

  private void notifyListeners(AppState state) {
    listeners.forEach(listener -> listener.accept(state));
  }

  private void handleDispatchFailure(Throwable throwable) {
    LOGGER.error("AppStore dispatch failed", throwable);
  }
}
