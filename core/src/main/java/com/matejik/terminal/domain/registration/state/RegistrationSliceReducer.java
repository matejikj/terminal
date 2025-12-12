package com.matejik.terminal.domain.registration.state;

public final class RegistrationSliceReducer {

  private RegistrationSliceReducer() {}

  public static RegistrationSlice reduce(RegistrationSlice slice, RegistrationAction action) {
    return switch (action) {
      case RegistrationAction.UpdateStatus update -> slice.withStatus(
          update.status(), update.accountLabel(), update.message(), update.occurredAt());
    };
  }
}
