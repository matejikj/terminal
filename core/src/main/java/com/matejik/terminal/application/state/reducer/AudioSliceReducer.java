package com.matejik.terminal.application.state.reducer;

import com.matejik.terminal.application.state.AudioSlice;
import com.matejik.terminal.application.state.actions.AudioAction;

final class AudioSliceReducer {

  private AudioSliceReducer() {}

  static AudioSlice reduce(AudioSlice slice, AudioAction action) {
    return switch (action) {
      case AudioAction.ReplaceDeviceLists replace -> slice.withDevices(
          replace.inputs(), replace.outputs());
      case AudioAction.SelectDevice select -> select.type().equals(AudioSlice.AudioDeviceType.INPUT)
          ? slice.withSelectedInput(select.deviceId())
          : slice.withSelectedOutput(select.deviceId());
      case AudioAction.ChangeRoute change -> slice.withRoute(change.route());
      case AudioAction.UpdateMute mute -> slice.withMuted(mute.muted());
    };
  }
}
