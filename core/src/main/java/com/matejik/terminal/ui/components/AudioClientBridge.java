package com.matejik.terminal.ui.components;

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
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

  @ClientCallable
  private void reportDevices(List<Map<String, String>> inputs, List<Map<String, String>> outputs) {
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

  private List<AudioDeviceSnapshot> mapSnapshots(List<Map<String, String>> payload) {
    return payload.stream()
        .map(
            entry ->
                new AudioDeviceSnapshot(
                    entry.getOrDefault("id", ""),
                    entry.getOrDefault("label", ""),
                    parseType(entry.getOrDefault("type", "INPUT"))))
        .toList();
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
