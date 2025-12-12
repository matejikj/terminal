package com.matejik.terminal.infrastructure.audio;

import com.matejik.terminal.application.concurrent.SharedBackendPool;
import com.matejik.terminal.domain.audio.AudioDeviceSnapshot;
import com.matejik.terminal.domain.audio.AudioDeviceType;
import com.matejik.terminal.domain.audio.AudioRoute;
import com.matejik.terminal.domain.audio.event.AudioDomainEvent;
import com.matejik.terminal.ui.component.AudioClientBridge;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import com.vaadin.flow.shared.Registration;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@VaadinSessionScope
public class AudioDeviceAdapter {

  private static final Logger LOGGER = LoggerFactory.getLogger(AudioDeviceAdapter.class);

  private final ApplicationEventPublisher events;
  private final SharedBackendPool backendPool;
  private final AtomicReference<AudioRoute> currentRoute =
      new AtomicReference<>(AudioRoute.SYSTEM_DEFAULT);
  private final AtomicReference<AudioClientBridge> clientBridge = new AtomicReference<>();
  private final CopyOnWriteArrayList<Consumer<Boolean>> micTestListeners =
      new CopyOnWriteArrayList<>();
  private final CopyOnWriteArrayList<Consumer<Boolean>> micMuteListeners =
      new CopyOnWriteArrayList<>();
  private final AtomicBoolean outputMuted = new AtomicBoolean(false);
  private final AtomicBoolean microphoneMuted = new AtomicBoolean(false);
  private final AtomicReference<Double> outputVolume = new AtomicReference<>(1d);

  public AudioDeviceAdapter(ApplicationEventPublisher events, SharedBackendPool backendPool) {
    this.events = events;
    this.backendPool = backendPool;
  }

  public void refreshDevices() {
    backendPool.runAsync(() -> sendCommand(AudioClientBridge::requestDeviceList));
  }

  public void selectDevice(AudioDeviceType type, String deviceId) {
    backendPool.runAsync(() -> sendCommand(bridge -> bridge.selectDevice(type, deviceId)));
  }

  public void changeRoute(AudioRoute route) {
    backendPool.runAsync(
        () -> {
          LOGGER.info("Requesting audio route {}", route);
          currentRoute.set(route);
          sendCommand(bridge -> bridge.setRoute(route));
        });
  }

  public void mute(boolean muted) {
    outputMuted.set(muted);
    backendPool.runAsync(() -> sendCommand(bridge -> bridge.setMute(muted)));
  }

  public void playTestSound() {
    playTestSoundOn(null);
  }

  public void playTestSoundOn(String deviceId) {
    if (outputMuted.get()) {
      LOGGER.debug("Skipping playback because audio output is muted");
      return;
    }
    backendPool.runAsync(() -> sendCommand(bridge -> bridge.playTestSound(deviceId)));
  }

  public void setOutputVolume(double volume) {
    var clamped = Math.max(0d, Math.min(1d, volume));
    outputVolume.set(clamped);
    backendPool.runAsync(() -> sendCommand(bridge -> bridge.setOutputVolume(clamped)));
  }

  public double currentOutputVolume() {
    return outputVolume.get();
  }

  public void startMicTest() {
    if (microphoneMuted.get()) {
      LOGGER.debug("Skipping microphone test because microphone is muted");
      notifyMicTestState(false);
      return;
    }
    backendPool.runAsync(() -> sendCommand(AudioClientBridge::startMicTest));
  }

  public void stopMicTest() {
    backendPool.runAsync(() -> sendCommand(AudioClientBridge::stopMicTest));
  }

  public void muteMicrophone(boolean muted) {
    microphoneMuted.set(muted);
    backendPool.runAsync(() -> sendCommand(bridge -> bridge.setMicrophoneMute(muted)));
    micMuteListeners.forEach(listener -> listener.accept(muted));
  }

  public void registerClient(AudioClientBridge bridge) {
    clientBridge.set(bridge);
    refreshDevices();
    sendCommand(b -> b.setOutputVolume(outputVolume.get()));
    sendCommand(b -> b.setMicrophoneMute(microphoneMuted.get()));
  }

  public void unregisterClient(AudioClientBridge bridge) {
    clientBridge.compareAndSet(bridge, null);
  }

  public void handleClientDevices(
      java.util.List<AudioDeviceSnapshot> inputs, java.util.List<AudioDeviceSnapshot> outputs) {
    LOGGER.info(
        "Received client audio device list inputs={} outputs={}", inputs.size(), outputs.size());
    events.publishEvent(new AudioDomainEvent.ClientDeviceListChanged(inputs, outputs));
    events.publishEvent(new AudioDomainEvent.DeviceListChanged(inputs, outputs));
  }

  public void handleClientSelection(AudioDeviceType type, String deviceId) {
    LOGGER.info("Client selected {} device {}", type, deviceId);
    events.publishEvent(new AudioDomainEvent.DeviceSelected(type, deviceId));
  }

  public void handleClientRoute(AudioRoute route) {
    currentRoute.set(route);
    events.publishEvent(new AudioDomainEvent.ClientRouteChanged(route));
    events.publishEvent(new AudioDomainEvent.RouteChanged(route));
  }

  public void handleClientMute(boolean muted) {
    outputMuted.set(muted);
    events.publishEvent(new AudioDomainEvent.ClientMuteStateChanged(muted));
    events.publishEvent(new AudioDomainEvent.MuteChanged(muted));
  }

  public void handleClientError(String message) {
    LOGGER.warn("Client audio error: {}", message);
  }

  public void handleMicTestState(boolean recording) {
    LOGGER.info("Microphone test recording state={}", recording);
    notifyMicTestState(recording);
  }

  public void handleClientMicrophoneMute(boolean muted) {
    microphoneMuted.set(muted);
    micMuteListeners.forEach(listener -> listener.accept(muted));
  }

  public Registration addMicTestStateListener(Consumer<Boolean> listener) {
    micTestListeners.add(listener);
    listener.accept(false);
    return () -> micTestListeners.remove(listener);
  }

  public Registration addMicrophoneMuteListener(Consumer<Boolean> listener) {
    micMuteListeners.add(listener);
    listener.accept(microphoneMuted.get());
    return () -> micMuteListeners.remove(listener);
  }

  private void notifyMicTestState(boolean recording) {
    micTestListeners.forEach(listener -> listener.accept(recording));
  }

  private void sendCommand(Consumer<AudioClientBridge> command) {
    var bridge = clientBridge.get();
    if (bridge == null) {
      LOGGER.debug("No audio client bridge attached, command skipped");
      return;
    }
    bridge
        .getUI()
        .ifPresent(
            ui ->
                ui.access(
                    () -> {
                      command.accept(bridge);
                    }));
  }
}
