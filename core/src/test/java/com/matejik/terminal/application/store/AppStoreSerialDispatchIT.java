package com.matejik.terminal.application.store;

import static org.assertj.core.api.Assertions.assertThat;

import com.matejik.terminal.application.state.AppState;
import com.matejik.terminal.application.state.CallSlice.CallPhase;
import com.matejik.terminal.application.state.CallSlice.CallView;
import com.matejik.terminal.application.state.actions.CallAction;
import com.matejik.terminal.common.concurrent.SerialExecutor;
import com.vaadin.flow.shared.Registration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

final class AppStoreSerialDispatchIT {

  private ExecutorService backend;

  @AfterEach
  void tearDown() {
    if (backend != null) {
      backend.shutdownNow();
    }
  }

  @Test
  void serialDispatchKeepsSubmissionOrder() throws Exception {
    var store = createStore();
    var ids =
        IntStream.range(0, 200).mapToObj(index -> "call-" + index).collect(Collectors.toList());
    Collections.shuffle(ids);
    var dispatchOrder = new CopyOnWriteArrayList<String>();
    var callerPool = Executors.newFixedThreadPool(8);
    var start = new CountDownLatch(1);
    List<Future<CompletableFuture<AppState>>> results = new ArrayList<>();
    for (String id : ids) {
      results.add(
          callerPool.submit(
              () -> {
                start.await();
                dispatchOrder.add(id);
                return store.dispatch(new CallAction.UpsertCall(newCall(id)));
              }));
    }
    start.countDown();
    var completions = collectCompletions(results);
    CompletableFuture.allOf(completions).get(10, TimeUnit.SECONDS);
    callerPool.shutdownNow();

    var finalStateIds =
        store.snapshot().callSlice().activeCalls().stream().map(CallView::id).toList();
    assertThat(finalStateIds).containsExactlyElementsOf(dispatchOrder);
  }

  @Test
  void subscribersReceiveUpdatesInDispatchOrder() throws Exception {
    var store = createStore();
    var baseCalls = IntStream.range(0, 40).mapToObj(index -> newCall("call-" + index)).toList();
    store.dispatch(new CallAction.ReplaceActiveCalls(baseCalls)).get(5, TimeUnit.SECONDS);

    var updates = new CopyOnWriteArrayList<String>();
    Registration subscription =
        store.subscribe(state -> updates.add(state.callSlice().selectedCallId()));

    var selectOrder = buildSelectionOrder(baseCalls.stream().map(CallView::id).toList(), 5);
    var callerPool = Executors.newFixedThreadPool(6);
    var start = new CountDownLatch(1);
    List<Future<CompletableFuture<AppState>>> results = new ArrayList<>();
    for (String id : selectOrder) {
      results.add(
          callerPool.submit(
              () -> {
                start.await();
                return store.dispatch(new CallAction.SelectCall(id));
              }));
    }
    start.countDown();
    var completions = collectCompletions(results);
    CompletableFuture.allOf(completions).get(10, TimeUnit.SECONDS);
    subscription.remove();
    callerPool.shutdownNow();

    assertThat(updates).isNotEmpty();
    // First update originates from eager subscription
    updates.remove(0);
    assertThat(updates).containsExactlyElementsOf(selectOrder);
    assertThat(store.snapshot().callSlice().selectedCallId())
        .isEqualTo(selectOrder.get(selectOrder.size() - 1));
  }

  private CompletableFuture<?>[] collectCompletions(
      List<Future<CompletableFuture<AppState>>> results) throws Exception {
    var completions = new ArrayList<CompletableFuture<?>>();
    for (var result : results) {
      completions.add(result.get(5, TimeUnit.SECONDS));
    }
    return completions.toArray(CompletableFuture[]::new);
  }

  private AppStore createStore() {
    backend = Executors.newFixedThreadPool(8);
    SerialActionDispatcher factory = errorHandler -> new SerialExecutor(backend, errorHandler);
    return new AppStore(factory);
  }

  private static CallView newCall(String id) {
    return new CallView(id, "Remote " + id, CallPhase.DIALING, Instant.now());
  }

  private static List<String> buildSelectionOrder(List<String> baseIds, int rounds) {
    var order = new ArrayList<String>();
    for (int round = 0; round < rounds; round++) {
      var chunk = new ArrayList<>(baseIds);
      Collections.shuffle(chunk);
      if (!order.isEmpty() && order.get(order.size() - 1).equals(chunk.get(0))) {
        Collections.swap(chunk, 0, chunk.size() - 1);
      }
      order.addAll(chunk);
    }
    return order;
  }
}
