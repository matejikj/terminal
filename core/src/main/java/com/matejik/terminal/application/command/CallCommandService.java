package com.matejik.terminal.application.command;

import com.matejik.terminal.application.state.CallSlice.CallPhase;
import com.matejik.terminal.application.state.CallSlice.CallView;
import com.matejik.terminal.application.state.actions.CallAction;
import com.matejik.terminal.application.store.AppStore;
import com.matejik.terminal.domain.call.CallSnapshot;
import com.matejik.terminal.domain.call.CallStatus;
import com.matejik.terminal.domain.call.event.CallDomainEvent;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@VaadinSessionScope
public class CallCommandService {

  private static final Logger LOGGER = LoggerFactory.getLogger(CallCommandService.class);

  private final CallControlPort callControlPort;
  private final AppStore appStore;

  public CallCommandService(CallControlPort callControlPort, AppStore appStore) {
    this.callControlPort = callControlPort;
    this.appStore = appStore;
  }

  public void startCall(String destination) {
    callControlPort.startOutboundCall(destination);
  }

  public void hangUp(String callId) {
    callControlPort.hangUp(callId);
  }

  public void answer(String callId) {
    callControlPort.answer(callId);
  }

  @EventListener
  public void onOutgoingCall(CallDomainEvent.OutgoingCallRequested event) {
    project(appStore.dispatch(new CallAction.UpsertCall(toView(event.snapshot()))));
  }

  @EventListener
  public void onIncomingCall(CallDomainEvent.IncomingCallDetected event) {
    project(appStore.dispatch(new CallAction.UpsertCall(toView(event.snapshot()))));
  }

  @EventListener
  public void onCallStateChanged(CallDomainEvent.CallStateChanged event) {
    project(appStore.dispatch(new CallAction.UpsertCall(toView(event.snapshot()))));
  }

  @EventListener
  public void onCallTerminated(CallDomainEvent.CallTerminated event) {
    project(appStore.dispatch(new CallAction.RemoveCall(event.snapshot().callId())));
  }

  private void project(CompletableFuture<?> future) {
    future.exceptionally(
        error -> {
          LOGGER.error("Call state projection failed", error);
          return null;
        });
  }

  private CallView toView(CallSnapshot snapshot) {
    return new CallView(
        snapshot.callId(),
        snapshot.remoteAddress(),
        toPhase(snapshot.status()),
        snapshot.startedAt());
  }

  private CallPhase toPhase(CallStatus status) {
    return switch (status) {
      case IDLE -> CallPhase.IDLE;
      case DIALING -> CallPhase.DIALING;
      case RINGING -> CallPhase.RINGING;
      case ACTIVE -> CallPhase.ACTIVE;
      case HOLD -> CallPhase.HOLD;
      case MUTED -> CallPhase.MUTED;
      case ENDED, FAILED -> CallPhase.ENDED;
    };
  }
}
