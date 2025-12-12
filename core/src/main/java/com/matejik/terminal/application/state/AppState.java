package com.matejik.terminal.application.state;

import java.util.Objects;

/**
 * Root immutable snapshot shared across the Vaadin UI. All slices must remain immutable to allow
 * optimistic rendering and thread-safe sharing between subscribers.
 */
public record AppState(
    CallSlice callSlice, AudioSlice audioSlice, RegistrationSlice registrationSlice) {

  public AppState {
    Objects.requireNonNull(callSlice, "callSlice");
    Objects.requireNonNull(audioSlice, "audioSlice");
    Objects.requireNonNull(registrationSlice, "registrationSlice");
  }

  public static AppState initial() {
    return new AppState(CallSlice.initial(), AudioSlice.initial(), RegistrationSlice.initial());
  }

  public AppState withCallSlice(CallSlice updated) {
    return new AppState(Objects.requireNonNull(updated, "updated"), audioSlice, registrationSlice);
  }

  public AppState withAudioSlice(AudioSlice updated) {
    return new AppState(callSlice, Objects.requireNonNull(updated, "updated"), registrationSlice);
  }

  public AppState withRegistrationSlice(RegistrationSlice updated) {
    return new AppState(callSlice, audioSlice, Objects.requireNonNull(updated, "updated"));
  }
}
