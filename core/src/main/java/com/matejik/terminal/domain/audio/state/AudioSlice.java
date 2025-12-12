package com.matejik.terminal.domain.audio.state;

import java.util.List;
import java.util.Objects;

public record AudioSlice(
    List<AudioDevice> inputDevices,
    List<AudioDevice> outputDevices,
    String selectedInputDeviceId,
    String primaryOutputDeviceId,
    String secondaryOutputDeviceId,
    AudioRoute activeRoute,
    boolean muted) {

  public AudioSlice {
    Objects.requireNonNull(inputDevices, "inputDevices");
    Objects.requireNonNull(outputDevices, "outputDevices");
    Objects.requireNonNull(activeRoute, "activeRoute");
    inputDevices = List.copyOf(inputDevices);
    outputDevices = List.copyOf(outputDevices);
  }

  public static AudioSlice initial() {
    return new AudioSlice(List.of(), List.of(), null, null, null, AudioRoute.SYSTEM_DEFAULT, false);
  }

  public boolean hasInputDevice(String deviceId) {
    return inputDevices.stream().anyMatch(device -> device.id().equals(deviceId));
  }

  public boolean hasOutputDevice(String deviceId) {
    return outputDevices.stream().anyMatch(device -> device.id().equals(deviceId));
  }

  public AudioSlice withDevices(List<AudioDevice> inputs, List<AudioDevice> outputs) {
    var nextInputs = List.copyOf(inputs);
    var nextOutputs = List.copyOf(outputs);
    var nextInputSelection = pickSelection(nextInputs, selectedInputDeviceId);
    var nextPrimarySelection = pickSelection(nextOutputs, primaryOutputDeviceId);
    var nextSecondarySelection =
        pickSecondarySelection(nextOutputs, secondaryOutputDeviceId, nextPrimarySelection);
    return new AudioSlice(
        nextInputs,
        nextOutputs,
        nextInputSelection,
        nextPrimarySelection,
        nextSecondarySelection,
        activeRoute,
        muted);
  }

  public AudioSlice withSelectedInput(String deviceId) {
    if (deviceId == null || !hasInputDevice(deviceId)) {
      return this;
    }
    if (Objects.equals(deviceId, selectedInputDeviceId)) {
      return this;
    }
    return new AudioSlice(
        inputDevices,
        outputDevices,
        deviceId,
        primaryOutputDeviceId,
        secondaryOutputDeviceId,
        activeRoute,
        muted);
  }

  public AudioSlice withPrimaryOutput(String deviceId) {
    if (deviceId != null && !hasOutputDevice(deviceId)) {
      return this;
    }
    if (Objects.equals(deviceId, primaryOutputDeviceId)) {
      return this;
    }
    return new AudioSlice(
        inputDevices,
        outputDevices,
        selectedInputDeviceId,
        deviceId,
        secondaryOutputDeviceId,
        activeRoute,
        muted);
  }

  public AudioSlice withSecondaryOutput(String deviceId) {
    if (deviceId != null && !hasOutputDevice(deviceId)) {
      return this;
    }
    if (Objects.equals(deviceId, secondaryOutputDeviceId)) {
      return this;
    }
    return new AudioSlice(
        inputDevices,
        outputDevices,
        selectedInputDeviceId,
        primaryOutputDeviceId,
        deviceId,
        activeRoute,
        muted);
  }

  public AudioSlice withRoute(AudioRoute route) {
    if (route == null || route == activeRoute) {
      return this;
    }
    return new AudioSlice(
        inputDevices,
        outputDevices,
        selectedInputDeviceId,
        primaryOutputDeviceId,
        secondaryOutputDeviceId,
        route,
        muted);
  }

  public AudioSlice withMuted(boolean value) {
    if (muted == value) {
      return this;
    }
    return new AudioSlice(
        inputDevices,
        outputDevices,
        selectedInputDeviceId,
        primaryOutputDeviceId,
        secondaryOutputDeviceId,
        activeRoute,
        value);
  }

  private static String pickSelection(List<AudioDevice> devices, String preferred) {
    if (preferred != null) {
      var exists = devices.stream().anyMatch(device -> device.id().equals(preferred));
      if (exists) {
        return preferred;
      }
    }
    return devices.isEmpty() ? null : devices.get(0).id();
  }

  private static String pickSecondarySelection(
      List<AudioDevice> devices, String preferred, String resolvedPrimary) {
    if (preferred != null) {
      var exists =
          devices.stream()
              .map(AudioDevice::id)
              .filter(id -> !Objects.equals(id, resolvedPrimary))
              .anyMatch(id -> id.equals(preferred));
      if (exists) {
        return preferred;
      }
    }
    return devices.stream()
        .map(AudioDevice::id)
        .filter(id -> !Objects.equals(id, resolvedPrimary))
        .findFirst()
        .orElse(null);
  }

  public record AudioDevice(String id, String label, AudioDeviceType type) {

    public AudioDevice {
      Objects.requireNonNull(id, "id");
      Objects.requireNonNull(label, "label");
      Objects.requireNonNull(type, "type");
    }
  }

  public enum AudioDeviceType {
    INPUT,
    OUTPUT
  }

  public enum AudioRoute {
    SYSTEM_DEFAULT,
    SPEAKER,
    HEADSET
  }
}
