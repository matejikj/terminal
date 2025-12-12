package com.matejik.terminal.domain.call;

import java.time.Instant;
import java.util.Objects;

public record CallSnapshot(
    String callId,
    String remoteAddress,
    CallDirection direction,
    CallStatus status,
    Instant startedAt) {

  public CallSnapshot {
    Objects.requireNonNull(callId, "callId");
    Objects.requireNonNull(remoteAddress, "remoteAddress");
    Objects.requireNonNull(direction, "direction");
    Objects.requireNonNull(status, "status");
    startedAt = startedAt == null ? Instant.EPOCH : startedAt;
  }
}
