package com.matejik.terminal.application.command;

import static org.assertj.core.api.Assertions.assertThat;

import com.matejik.terminal.application.state.CallSlice.CallView;
import com.matejik.terminal.application.store.AppStore;
import com.matejik.terminal.application.store.SerialActionDispatcher;
import com.matejik.terminal.common.concurrent.SerialExecutor;
import com.matejik.terminal.domain.call.CallDirection;
import com.matejik.terminal.domain.call.CallSnapshot;
import com.matejik.terminal.domain.call.CallStatus;
import com.matejik.terminal.domain.call.event.CallDomainEvent;
import java.time.Instant;
import java.util.concurrent.Executor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class CallCommandServiceTest {

  private TestCallPort callPort;
  private AppStore store;
  private CallCommandService service;

  @BeforeEach
  void setUp() {
    callPort = new TestCallPort();
    store =
        new AppStore(
            new SerialActionDispatcher() {
              @Override
              public SerialExecutor create(java.util.function.Consumer<Throwable> errorHandler) {
                Executor executor = Runnable::run;
                return new SerialExecutor(executor, errorHandler);
              }
            });
    service = new CallCommandService(callPort, store);
  }

  @Test
  void startCallDelegatesToAdapter() {
    service.startCall("123");
    assertThat(callPort.lastStarted).isEqualTo("123");
  }

  @Test
  void projectsCallSnapshotOnEvents() {
    var snapshot =
        new CallSnapshot(
            "call-1", "Remote", CallDirection.OUTBOUND, CallStatus.RINGING, Instant.now());
    service.onOutgoingCall(new CallDomainEvent.OutgoingCallRequested(snapshot));
    var state = store.snapshot();
    assertThat(state.callSlice().activeCalls()).hasSize(1);
    CallView view = state.callSlice().activeCalls().get(0);
    assertThat(view.id()).isEqualTo("call-1");
    assertThat(view.remoteAddress()).isEqualTo("Remote");
  }

  @Test
  void removesCallWhenTerminated() {
    var snapshot =
        new CallSnapshot(
            "call-2", "Remote 2", CallDirection.OUTBOUND, CallStatus.ACTIVE, Instant.now());
    service.onCallStateChanged(new CallDomainEvent.CallStateChanged(snapshot));
    service.onCallTerminated(new CallDomainEvent.CallTerminated(snapshot));
    assertThat(store.snapshot().callSlice().activeCalls()).isEmpty();
  }

  private static final class TestCallPort implements CallControlPort {

    private String lastStarted;
    private String lastHangup;
    private String lastAnswered;

    @Override
    public void startOutboundCall(String destination) {
      this.lastStarted = destination;
    }

    @Override
    public void hangUp(String callId) {
      this.lastHangup = callId;
    }

    @Override
    public void answer(String callId) {
      this.lastAnswered = callId;
    }
  }
}
