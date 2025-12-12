package com.matejik.terminal.application.state.reducer;

import com.matejik.terminal.application.state.AppState;
import com.matejik.terminal.application.state.actions.AppAction;
import com.matejik.terminal.domain.audio.state.AudioAction;
import com.matejik.terminal.domain.audio.state.AudioSliceReducer;
import com.matejik.terminal.domain.call.state.CallAction;
import com.matejik.terminal.domain.call.state.CallSliceReducer;
import com.matejik.terminal.domain.registration.state.RegistrationAction;
import com.matejik.terminal.domain.registration.state.RegistrationSliceReducer;

public final class AppStateReducer {

  private AppStateReducer() {}

  public static AppState reduce(AppState state, AppAction action) {
    if (action instanceof CallAction callAction) {
      return state.withCallSlice(CallSliceReducer.reduce(state.callSlice(), callAction));
    }
    if (action instanceof AudioAction audioAction) {
      return state.withAudioSlice(AudioSliceReducer.reduce(state.audioSlice(), audioAction));
    }
    if (action instanceof RegistrationAction registrationAction) {
      return state.withRegistrationSlice(
          RegistrationSliceReducer.reduce(state.registrationSlice(), registrationAction));
    }
    throw new IllegalArgumentException("Unsupported action type: " + action);
  }
}
