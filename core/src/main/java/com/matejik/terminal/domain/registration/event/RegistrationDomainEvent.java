package com.matejik.terminal.domain.registration.event;

import com.matejik.terminal.domain.event.DomainEvent;
import com.matejik.terminal.domain.registration.RegistrationStatus;

public sealed interface RegistrationDomainEvent extends DomainEvent
    permits RegistrationDomainEvent.ConnectionChanged {

  record ConnectionChanged(RegistrationStatus status, String accountLabel, String message)
      implements RegistrationDomainEvent {}
}
