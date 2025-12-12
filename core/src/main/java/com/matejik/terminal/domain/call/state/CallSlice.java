package com.matejik.terminal.domain.call.state;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record CallSlice(List<CallView> activeCalls, String selectedCallId) {

  public CallSlice {
    Objects.requireNonNull(activeCalls, "activeCalls");
    activeCalls = List.copyOf(activeCalls);
  }

  public static CallSlice initial() {
    return new CallSlice(List.of(), null);
  }

  public CallSlice withActiveCalls(List<CallView> calls) {
    return replaceAll(this, calls);
  }

  public CallSlice withSelectedCallId(String callId) {
    if (callId == null) {
      return new CallSlice(activeCalls, null);
    }
    var exists = activeCalls.stream().anyMatch(call -> Objects.equals(call.id(), callId));
    if (!exists) {
      return this;
    }
    if (Objects.equals(selectedCallId, callId)) {
      return this;
    }
    return new CallSlice(activeCalls, callId);
  }

  public Optional<CallView> selectedCall() {
    if (selectedCallId == null) {
      return Optional.empty();
    }
    return activeCalls.stream().filter(call -> call.id().equals(selectedCallId)).findFirst();
  }

  public boolean hasCall(String callId) {
    return activeCalls.stream().anyMatch(call -> call.id().equals(callId));
  }

  public static CallSlice replaceAll(CallSlice slice, List<CallView> calls) {
    var copy = List.copyOf(calls);
    return new CallSlice(copy, pickSelectedCallId(copy, slice.selectedCallId));
  }

  public static CallSlice upsert(CallSlice slice, CallView call) {
    var next = new ArrayList<>(slice.activeCalls);
    for (int i = 0; i < next.size(); i++) {
      if (next.get(i).id().equals(call.id())) {
        next.set(i, call);
        return new CallSlice(next, pickSelectedCallId(next, slice.selectedCallId));
      }
    }
    next.add(call);
    return new CallSlice(next, pickSelectedCallId(next, slice.selectedCallId));
  }

  public static CallSlice remove(CallSlice slice, String callId) {
    if (!slice.hasCall(callId)) {
      return slice;
    }
    var next = new ArrayList<>(slice.activeCalls);
    next.removeIf(call -> call.id().equals(callId));
    return new CallSlice(next, pickSelectedCallId(next, slice.selectedCallId));
  }

  private static String pickSelectedCallId(List<CallView> calls, String preferred) {
    if (preferred != null) {
      var exists = calls.stream().anyMatch(call -> call.id().equals(preferred));
      if (exists) {
        return preferred;
      }
    }
    return calls.isEmpty() ? null : calls.get(0).id();
  }

  public record CallView(String id, String remoteAddress, CallPhase phase, Instant startedAt) {

    public CallView {
      Objects.requireNonNull(id, "id");
      Objects.requireNonNull(remoteAddress, "remoteAddress");
      Objects.requireNonNull(phase, "phase");
      startedAt = startedAt == null ? Instant.EPOCH : startedAt;
    }
  }

  public enum CallPhase {
    IDLE,
    DIALING,
    RINGING,
    ACTIVE,
    HOLD,
    MUTED,
    ENDED
  }
}
