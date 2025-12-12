package com.matejik.terminal.domain.audio.event;

import com.matejik.terminal.domain.audio.AudioDeviceSnapshot;
import com.matejik.terminal.domain.audio.AudioDeviceType;
import com.matejik.terminal.domain.audio.AudioRoute;
import com.matejik.terminal.domain.event.DomainEvent;
import java.util.List;

public sealed interface AudioDomainEvent extends DomainEvent
    permits AudioDomainEvent.DeviceListChanged,
        AudioDomainEvent.DeviceSelected,
        AudioDomainEvent.RouteChanged,
        AudioDomainEvent.MuteChanged,
        AudioDomainEvent.ClientDeviceListChanged,
        AudioDomainEvent.ClientRouteChanged,
        AudioDomainEvent.ClientMuteStateChanged {

  record DeviceListChanged(List<AudioDeviceSnapshot> inputs, List<AudioDeviceSnapshot> outputs)
      implements AudioDomainEvent {}

  record DeviceSelected(AudioDeviceType type, String deviceId) implements AudioDomainEvent {}

  record RouteChanged(AudioRoute route) implements AudioDomainEvent {}

  record MuteChanged(boolean muted) implements AudioDomainEvent {}

  record ClientDeviceListChanged(
      List<AudioDeviceSnapshot> inputs, List<AudioDeviceSnapshot> outputs)
      implements AudioDomainEvent {}

  record ClientRouteChanged(AudioRoute route) implements AudioDomainEvent {}

  record ClientMuteStateChanged(boolean muted) implements AudioDomainEvent {}
}
