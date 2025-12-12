package com.matejik.terminal.application.state;

import com.matejik.terminal.domain.registration.RegistrationStatus;
import java.time.Instant;
import java.util.Objects;

public record RegistrationSlice(
    RegistrationStatus status, String accountLabel, String message, Instant updatedAt) {

  public RegistrationSlice {
    Objects.requireNonNull(status, "status");
    accountLabel = accountLabel == null ? "" : accountLabel;
    message = message == null ? "" : message;
    updatedAt = updatedAt == null ? Instant.EPOCH : updatedAt;
  }

  public static RegistrationSlice initial() {
    return new RegistrationSlice(RegistrationStatus.UNREGISTERED, "", "", Instant.EPOCH);
  }

  public RegistrationSlice withStatus(
      RegistrationStatus status, String accountLabel, String message, Instant when) {
    return new RegistrationSlice(status, accountLabel, message, when);
  }
}
