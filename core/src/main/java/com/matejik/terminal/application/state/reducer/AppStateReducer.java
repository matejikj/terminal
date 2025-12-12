package com.matejik.terminal.application.state.reducer;

import com.matejik.terminal.application.state.AppState;
import com.matejik.terminal.application.state.actions.AppAction;
import com.matejik.terminal.application.state.actions.AudioAction;
import com.matejik.terminal.application.state.actions.CallAction;
import com.matejik.terminal.application.state.actions.RegistrationAction;

public final class AppStateReducer {

  private AppStateReducer() {}

  public static AppState reduce(AppState state, AppAction action) {
    return switch (action) {
      case CallAction callAction -> state.withCallSlice(
          CallSliceReducer.reduce(state.callSlice(), callAction));
      case AudioAction audioAction -> state.withAudioSlice(
          AudioSliceReducer.reduce(state.audioSlice(), audioAction));
      case RegistrationAction registrationAction -> state.withRegistrationSlice(
          RegistrationSliceReducer.reduce(state.registrationSlice(), registrationAction));
    };
  }
}
