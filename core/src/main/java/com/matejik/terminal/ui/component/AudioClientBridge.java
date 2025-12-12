package com.matejik.terminal.ui.component;

import com.matejik.terminal.domain.audio.AudioDeviceSnapshot;
import com.matejik.terminal.domain.audio.AudioDeviceType;
import com.matejik.terminal.domain.audio.AudioRoute;
import com.matejik.terminal.infrastructure.audio.AudioDeviceAdapter;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import elemental.json.JsonValue;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Tag("audio-client-bridge")
@JsModule("./src/audio-client-bridge.ts")
public class AudioClientBridge extends Component {

  private final AudioDeviceAdapter audioDeviceAdapter;

  public AudioClientBridge(AudioDeviceAdapter audioDeviceAdapter) {
    this.audioDeviceAdapter = audioDeviceAdapter;
    getElement().getStyle().set("display", "none");
  }

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);
    audioDeviceAdapter.registerClient(this);
  }

  @Override
  protected void onDetach(DetachEvent detachEvent) {
    super.onDetach(detachEvent);
    audioDeviceAdapter.unregisterClient(this);
  }

  public void requestDeviceList() {
    getElement().callJsFunction("requestDeviceList");
  }

  public void selectDevice(AudioDeviceType type, String deviceId) {
    getElement().callJsFunction("selectDevice", type.name(), deviceId);
  }

  public void setRoute(AudioRoute route) {
    getElement().callJsFunction("setRoute", route.name());
  }

  public void setMute(boolean muted) {
    getElement().callJsFunction("setMute", muted);
  }

  public void playTestSound() {
    playTestSound(null);
  }

  public void playTestSound(String deviceId) {
    getElement().callJsFunction("playTestSound", deviceId);
  }

  public void startMicTest() {
    getElement().callJsFunction("startMicTest");
  }

  public void stopMicTest() {
    getElement().callJsFunction("stopMicTest");
  }

  public void setOutputVolume(double volume) {
    getElement().callJsFunction("setOutputVolume", volume);
  }

  public void setMicrophoneMute(boolean muted) {
    getElement().callJsFunction("setMicrophoneMute", muted);
  }

  @ClientCallable
  private void reportDevices(JsonArray inputs, JsonArray outputs) {
    audioDeviceAdapter.handleClientDevices(mapSnapshots(inputs), mapSnapshots(outputs));
  }

  @ClientCallable
  private void reportDeviceSelection(String type, String deviceId) {
    audioDeviceAdapter.handleClientSelection(parseType(type), deviceId);
  }

  @ClientCallable
  private void reportRoute(String route) {
    audioDeviceAdapter.handleClientRoute(parseRoute(route));
  }

  @ClientCallable
  private void reportMute(boolean muted) {
    audioDeviceAdapter.handleClientMute(muted);
  }

  @ClientCallable
  private void reportError(String message) {
    audioDeviceAdapter.handleClientError(message);
  }

  @ClientCallable
  private void reportMicTestState(boolean recording) {
    audioDeviceAdapter.handleMicTestState(recording);
  }

  @ClientCallable
  private void reportMicrophoneMute(boolean muted) {
    audioDeviceAdapter.handleClientMicrophoneMute(muted);
  }

  private List<AudioDeviceSnapshot> mapSnapshots(JsonArray payload) {
    if (payload == null || payload.length() == 0) {
      return List.of();
    }
    List<AudioDeviceSnapshot> devices = new ArrayList<>(payload.length());
    for (int index = 0; index < payload.length(); index++) {
      devices.add(mapSnapshot(payload.getObject(index)));
    }
    return devices;
  }

  private AudioDeviceSnapshot mapSnapshot(JsonObject entry) {
    return new AudioDeviceSnapshot(
        readString(entry, "id", ""),
        readString(entry, "label", ""),
        parseType(readString(entry, "type", "INPUT")));
  }

  private String readString(JsonObject entry, String key, String fallback) {
    if (entry == null || !entry.hasKey(key)) {
      return fallback;
    }
    JsonValue value = entry.get(key);
    return value == null ? fallback : value.asString();
  }

  private AudioDeviceType parseType(String raw) {
    if (raw == null) {
      return AudioDeviceType.INPUT;
    }
    return "OUTPUT".equalsIgnoreCase(raw) ? AudioDeviceType.OUTPUT : AudioDeviceType.INPUT;
  }

  private AudioRoute parseRoute(String raw) {
    if (raw == null) {
      return AudioRoute.SYSTEM_DEFAULT;
    }
    var normalized = raw.toUpperCase(Locale.ROOT);
    return switch (normalized) {
      case "SPEAKER" -> AudioRoute.SPEAKER;
      case "HEADSET" -> AudioRoute.HEADSET;
      default -> AudioRoute.SYSTEM_DEFAULT;
    };
  }
}
