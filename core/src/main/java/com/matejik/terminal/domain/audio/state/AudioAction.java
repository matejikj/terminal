package com.matejik.terminal.domain.audio.state;

import com.matejik.terminal.application.state.actions.AppAction;
import com.matejik.terminal.domain.audio.state.AudioSlice.AudioDevice;
import com.matejik.terminal.domain.audio.state.AudioSlice.AudioDeviceType;
import com.matejik.terminal.domain.audio.state.AudioSlice.AudioRoute;
import java.util.List;
import java.util.Objects;

public sealed interface AudioAction extends AppAction
    permits AudioAction.ReplaceDeviceLists,
        AudioAction.SelectDevice,
        AudioAction.ChangeRoute,
        AudioAction.UpdatePrimaryOutput,
        AudioAction.UpdateSecondaryOutput,
        AudioAction.UpdateMute {

  record ReplaceDeviceLists(List<AudioDevice> inputs, List<AudioDevice> outputs)
      implements AudioAction {

    public ReplaceDeviceLists {
      Objects.requireNonNull(inputs, "inputs");
      Objects.requireNonNull(outputs, "outputs");
    }
  }

  record SelectDevice(AudioDeviceType type, String deviceId) implements AudioAction {

    public SelectDevice {
      Objects.requireNonNull(type, "type");
      Objects.requireNonNull(deviceId, "deviceId");
    }
  }

  record ChangeRoute(AudioRoute route) implements AudioAction {

    public ChangeRoute {
      Objects.requireNonNull(route, "route");
    }
  }

  record UpdatePrimaryOutput(String deviceId) implements AudioAction {}

  record UpdateSecondaryOutput(String deviceId) implements AudioAction {}

  record UpdateMute(boolean muted) implements AudioAction {}
}
