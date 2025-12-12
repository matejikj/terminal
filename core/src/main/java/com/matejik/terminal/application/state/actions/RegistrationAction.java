package com.matejik.terminal.application.state.actions;

import com.matejik.terminal.domain.registration.RegistrationStatus;
import java.time.Instant;
import java.util.Objects;

public sealed interface RegistrationAction extends AppAction
    permits RegistrationAction.UpdateStatus {

  record UpdateStatus(
      RegistrationStatus status, String accountLabel, String message, Instant occurredAt)
      implements RegistrationAction {

    public UpdateStatus {
      Objects.requireNonNull(status, "status");
      occurredAt = occurredAt == null ? Instant.now() : occurredAt;
    }
  }
}
