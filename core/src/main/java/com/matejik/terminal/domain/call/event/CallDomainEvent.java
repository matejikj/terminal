package com.matejik.terminal.domain.call.event;

import com.matejik.terminal.domain.call.CallSnapshot;
import com.matejik.terminal.domain.event.DomainEvent;

public sealed interface CallDomainEvent extends DomainEvent
    permits CallDomainEvent.OutgoingCallRequested,
        CallDomainEvent.IncomingCallDetected,
        CallDomainEvent.CallStateChanged,
        CallDomainEvent.CallTerminated {

  record OutgoingCallRequested(CallSnapshot snapshot) implements CallDomainEvent {}

  record IncomingCallDetected(CallSnapshot snapshot) implements CallDomainEvent {}

  record CallStateChanged(CallSnapshot snapshot) implements CallDomainEvent {}

  record CallTerminated(CallSnapshot snapshot) implements CallDomainEvent {}
}
