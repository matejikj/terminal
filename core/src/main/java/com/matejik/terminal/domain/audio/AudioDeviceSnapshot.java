package com.matejik.terminal.domain.audio;

import java.util.Objects;

public record AudioDeviceSnapshot(String id, String label, AudioDeviceType type) {

  public AudioDeviceSnapshot {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(label, "label");
    Objects.requireNonNull(type, "type");
  }
}
