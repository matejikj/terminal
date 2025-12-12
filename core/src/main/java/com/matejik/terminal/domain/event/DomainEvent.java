package com.matejik.terminal.domain.event;

import java.time.Instant;

/**
 * Marker for domain notifications produced by infrastructure adapters or aggregates. Events should
 * be immutable and represent past facts.
 */
public interface DomainEvent {

  default Instant occurredAt() {
    return Instant.now();
  }
}
