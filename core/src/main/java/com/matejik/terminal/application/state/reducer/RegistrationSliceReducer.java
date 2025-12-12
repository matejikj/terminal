package com.matejik.terminal.application.state.reducer;

import com.matejik.terminal.application.state.RegistrationSlice;
import com.matejik.terminal.application.state.actions.RegistrationAction;

final class RegistrationSliceReducer {

  private RegistrationSliceReducer() {}

  static RegistrationSlice reduce(RegistrationSlice slice, RegistrationAction action) {
    return switch (action) {
      case RegistrationAction.UpdateStatus update -> slice.withStatus(
          update.status(), update.accountLabel(), update.message(), update.occurredAt());
    };
  }
}
