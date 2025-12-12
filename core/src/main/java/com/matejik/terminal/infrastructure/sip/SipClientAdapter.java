package com.matejik.terminal.infrastructure.sip;

import com.matejik.sip.SipAccountCredentials;
import com.matejik.sip.SipCall;
import com.matejik.sip.SipCallDirection;
import com.matejik.sip.SipCallState;
import com.matejik.sip.SipClient;
import com.matejik.sip.SipConnectionStatus;
import com.matejik.sip.SipEvent;
import com.matejik.sip.SipRegistration;
import com.matejik.terminal.domain.call.command.CallControlPort;
import com.matejik.terminal.domain.registration.command.RegistrationControlPort;
import com.matejik.terminal.application.concurrent.SharedBackendPool;
import com.matejik.terminal.domain.call.CallDirection;
import com.matejik.terminal.domain.call.CallSnapshot;
import com.matejik.terminal.domain.call.CallStatus;
import com.matejik.terminal.domain.call.event.CallDomainEvent;
import com.matejik.terminal.domain.registration.RegistrationStatus;
import com.matejik.terminal.domain.registration.event.RegistrationDomainEvent;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import jakarta.annotation.PreDestroy;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@VaadinSessionScope
public class SipClientAdapter implements CallControlPort, RegistrationControlPort {

  private static final Logger LOGGER = LoggerFactory.getLogger(SipClientAdapter.class);

  private final SipClient sipClient;
  private final ApplicationEventPublisher events;
  private final SipRegistration registration;
  private final SharedBackendPool backendPool;
  private final Map<String, SipCall> calls = new ConcurrentHashMap<>();

  public SipClientAdapter(
      SipClient sipClient, ApplicationEventPublisher events, SharedBackendPool backendPool) {
    this.sipClient = Objects.requireNonNull(sipClient, "sipClient");
    this.events = Objects.requireNonNull(events, "events");
    this.backendPool = Objects.requireNonNull(backendPool, "backendPool");
    this.registration = sipClient.addListener(this::handleEvent);
  }

  @Override
  public void startOutboundCall(String destination) {
    Objects.requireNonNull(destination, "destination");
    backendPool.runAsync(
        () -> {
          var call = sipClient.makeCall(destination);
          trackCall(call);
          var snapshot = toSnapshot(call);
          LOGGER.info("Outbound call requested {}", snapshot);
          events.publishEvent(new CallDomainEvent.OutgoingCallRequested(snapshot));
        });
  }

  @Override
  public void hangUp(String callId) {
    backendPool.runAsync(
        () ->
            locateCall(callId)
                .ifPresentOrElse(
                    call -> {
                      LOGGER.info("Hanging up call {}", call.id());
                      sipClient.hangupCall(call);
                    },
                    () -> LOGGER.warn("No call found for hangup {}", callId)));
  }

  @Override
  public void answer(String callId) {
    backendPool.runAsync(
        () ->
            locateCall(callId)
                .ifPresentOrElse(
                    call -> {
                      LOGGER.info("Answering call {}", call.id());
                      sipClient.answerCall(call);
                    },
                    () -> LOGGER.warn("No call found for answer {}", callId)));
  }

  @Override
  public void connect(SipAccountCredentials credentials) {
    backendPool.runAsync(() -> sipClient.connect(credentials));
  }

  @Override
  public void disconnect() {
    backendPool.runAsync(sipClient::disconnect);
  }

  private void handleEvent(SipEvent event) {
    switch (event) {
      case SipEvent.CallProgress callProgress -> handleCallProgress(callProgress.call());
      case SipEvent.CallTerminated callTerminated -> handleCallTerminated(callTerminated.call());
      case SipEvent.ConnectionChanged connection -> handleConnectionChange(
          connection.status(), connection.account().orElse(null));
      default -> LOGGER.debug("Ignoring SIP event {}", event);
    }
  }

  private void handleCallProgress(SipCall call) {
    trackCall(call);
    var snapshot = toSnapshot(call);
    if (call.direction() == SipCallDirection.INBOUND && call.state() == SipCallState.RINGING) {
      events.publishEvent(new CallDomainEvent.IncomingCallDetected(snapshot));
    } else {
      events.publishEvent(new CallDomainEvent.CallStateChanged(snapshot));
    }
  }

  private void handleCallTerminated(SipCall call) {
    calls.remove(call.id().toString());
    events.publishEvent(new CallDomainEvent.CallTerminated(toSnapshot(call)));
  }

  private void handleConnectionChange(SipConnectionStatus status, SipAccountCredentials account) {
    var accountLabel =
        account == null ? "" : account.displayName() + " (" + account.toSipUri() + ')';
    events.publishEvent(
        new RegistrationDomainEvent.ConnectionChanged(
            mapRegistration(status), accountLabel, status.name()));
  }

  private Optional<SipCall> locateCall(String callId) {
    if (callId == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(calls.get(callId));
  }

  private void trackCall(SipCall call) {
    calls.put(call.id().toString(), call);
  }

  private CallSnapshot toSnapshot(SipCall call) {
    var remote = Optional.ofNullable(call.remoteAddress()).orElse("Unknown");
    return new CallSnapshot(
        call.id().toString(),
        remote,
        mapDirection(call.direction()),
        mapStatus(call.state()),
        call.startedAt());
  }

  private static CallDirection mapDirection(SipCallDirection direction) {
    return switch (direction) {
      case INBOUND -> CallDirection.INBOUND;
      case OUTBOUND -> CallDirection.OUTBOUND;
    };
  }

  private static CallStatus mapStatus(SipCallState state) {
    return switch (state) {
      case CONNECTING -> CallStatus.DIALING;
      case RINGING -> CallStatus.RINGING;
      case IN_CALL -> CallStatus.ACTIVE;
      case ENDED -> CallStatus.ENDED;
      case FAILED -> CallStatus.FAILED;
    };
  }

  private static RegistrationStatus mapRegistration(SipConnectionStatus status) {
    return switch (status) {
      case CONNECTING -> RegistrationStatus.REGISTERING;
      case CONNECTED -> RegistrationStatus.REGISTERED;
      case DISCONNECTED -> RegistrationStatus.UNREGISTERED;
      case ERROR -> RegistrationStatus.ERROR;
    };
  }

  @PreDestroy
  void dispose() {
    try {
      registration.remove();
    } catch (Exception exception) {
      LOGGER.warn("Failed to dispose SIP registration", exception);
    }
  }
}
